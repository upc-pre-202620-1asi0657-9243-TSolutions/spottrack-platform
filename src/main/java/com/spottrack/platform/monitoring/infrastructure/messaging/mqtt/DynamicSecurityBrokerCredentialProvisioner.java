package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import com.spottrack.platform.monitoring.application.ports.BrokerCredentialProvisioner;
import com.spottrack.platform.monitoring.application.ports.BrokerProvisioningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Creates a device's MQTT account on every Mosquitto broker through the dynamic-security plugin.
 * <p>
 * The brokers are independent, so a credential is only usable if it exists on all of them.
 * Provisioning is therefore all-or-nothing: if any broker fails, the account is deleted from
 * every broker (including the one that failed, which may have applied the change before timing out).
 * The commands of one broker travel in one message and are idempotent as a unit, which is what
 * makes retrying after a transport error safe.
 * <p>
 * The secret and the commands that carry it are never logged nor included in exception messages.
 */
public class DynamicSecurityBrokerCredentialProvisioner implements BrokerCredentialProvisioner {
    private static final Logger log = LoggerFactory.getLogger(DynamicSecurityBrokerCredentialProvisioner.class);
    private static final Pattern DEVICE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");
    /** Accounts of the platform itself: a device must never be able to overwrite them. */
    static final Set<String> RESERVED_USERNAMES = Set.of("spottrack-backend", "spottrack-dynsec-admin");

    private final DynsecBrokerClient client;
    private final DynsecProvisionerSettings settings;

    DynamicSecurityBrokerCredentialProvisioner(DynsecBrokerClient client, DynsecProvisionerSettings settings) {
        this.client = client;
        this.settings = settings;
    }

    @Override
    public void provision(String deviceId, String secret) {
        requireValidDeviceId(deviceId);
        if (secret == null || secret.isBlank()) {
            throw new BrokerProvisioningException("The broker secret of device_id=" + deviceId + " is empty");
        }
        var commands = List.of(
                DynsecCommand.deleteClient(deviceId),
                DynsecCommand.createClient(deviceId, secret),
                DynsecCommand.addClientRole(deviceId, settings.sensorRole()));

        var failures = fanOut(commands, DynamicSecurityBrokerCredentialProvisioner::evaluateProvision);
        if (failures.isEmpty()) {
            log.info("Broker credential of device_id={} created on {} broker(s)", deviceId, settings.brokerUrls().size());
            return;
        }

        var rollbackFailures = fanOut(List.of(DynsecCommand.deleteClient(deviceId)),
                DynamicSecurityBrokerCredentialProvisioner::evaluateDelete);
        if (!rollbackFailures.isEmpty()) {
            log.error("Rollback of the broker credential of device_id={} failed on {}; revoke it again", deviceId,
                    describe(rollbackFailures));
        }
        throw new BrokerProvisioningException("Could not create the broker credential of device_id=" + deviceId
                + " on " + describe(failures) + (rollbackFailures.isEmpty()
                ? "; it was removed from every broker"
                : "; it may still exist on " + describe(rollbackFailures)));
    }

    @Override
    public void revoke(String deviceId) {
        requireValidDeviceId(deviceId);
        var failures = fanOut(List.of(DynsecCommand.deleteClient(deviceId)),
                DynamicSecurityBrokerCredentialProvisioner::evaluateDelete);
        if (failures.isEmpty()) {
            log.info("Broker credential of device_id={} removed from {} broker(s)", deviceId, settings.brokerUrls().size());
            return;
        }
        throw new BrokerProvisioningException("Could not remove the broker credential of device_id=" + deviceId
                + " from " + describe(failures));
    }

    private static void requireValidDeviceId(String deviceId) {
        if (deviceId == null || !DEVICE_ID.matcher(deviceId).matches() || RESERVED_USERNAMES.contains(deviceId)) {
            throw new BrokerProvisioningException("Invalid device_id for a broker credential");
        }
    }

    /** @return null when the results are what we expected, otherwise why not */
    private static String evaluateProvision(List<DynsecResult> results) {
        if (results.size() != 3) return "unexpected answer of the broker";
        var deleted = results.get(0);
        if (!deleted.ok() && !DynsecResult.CLIENT_NOT_FOUND.equals(deleted.error())) return "deleteClient: " + deleted.error();
        for (var result : results.subList(1, results.size())) {
            if (!result.ok()) return result.command() + ": " + result.error();
        }
        return null;
    }

    private static String evaluateDelete(List<DynsecResult> results) {
        if (results.size() != 1) return "unexpected answer of the broker";
        var result = results.get(0);
        if (result.ok() || DynsecResult.CLIENT_NOT_FOUND.equals(result.error())) return null;
        return "deleteClient: " + result.error();
    }

    /** Runs the commands on every broker in parallel; returns the brokers that failed with the reason. */
    private List<BrokerFailure> fanOut(List<DynsecCommand> commands, Function<List<DynsecResult>, String> evaluator) {
        var urls = settings.brokerUrls();
        List<Callable<String>> tasks = urls.stream()
                .<Callable<String>>map(url -> () -> runOnBroker(url, commands, evaluator))
                .toList();

        var failures = new ArrayList<BrokerFailure>();
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        try {
            var futures = executor.invokeAll(tasks, settings.totalTimeout().toMillis(), TimeUnit.MILLISECONDS);
            for (int i = 0; i < urls.size(); i++) {
                var future = futures.get(i);
                String reason;
                if (future.isCancelled()) {
                    reason = "timed out";
                } else {
                    try {
                        reason = future.get();
                    } catch (Exception e) {
                        reason = "unexpected error " + e.getClass().getSimpleName();
                    }
                }
                if (reason != null) failures.add(new BrokerFailure(urls.get(i), reason));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            urls.forEach(url -> failures.add(new BrokerFailure(url, "interrupted")));
        } finally {
            executor.shutdownNow();
        }
        return failures;
    }

    private String runOnBroker(String url, List<DynsecCommand> commands, Function<List<DynsecResult>, String> evaluator) {
        String lastError = "unreachable";
        for (int attempt = 1; attempt <= settings.maxAttempts(); attempt++) {
            try {
                return evaluator.apply(client.execute(url, commands));
            } catch (DynsecTransportException e) {
                lastError = e.getMessage();
                log.debug("Dynsec attempt {}/{} on {} failed: {}", attempt, settings.maxAttempts(), url, e.getMessage());
                if (attempt < settings.maxAttempts() && !pause()) return "interrupted";
            }
        }
        return lastError;
    }

    private boolean pause() {
        try {
            Thread.sleep(settings.retryDelay());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private static String describe(List<BrokerFailure> failures) {
        return String.join(", ", failures.stream().map(BrokerFailure::toString).toList());
    }

    private record BrokerFailure(String brokerUrl, String reason) {
        @Override
        public String toString() {
            return brokerUrl + " (" + reason + ")";
        }
    }
}
