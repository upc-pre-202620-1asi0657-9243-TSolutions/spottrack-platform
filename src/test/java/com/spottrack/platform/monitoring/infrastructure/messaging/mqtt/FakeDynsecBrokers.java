package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

/** In-memory brokers that apply dynamic-security commands the way Mosquitto answers them. */
class FakeDynsecBrokers implements DynsecBrokerClient {
    record Call(String brokerUrl, List<DynsecCommand> commands) {}

    private final Map<String, Map<String, String>> passwordsByBroker = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> rolesByBroker = new ConcurrentHashMap<>();
    private final Map<String, Supplier<DynsecTransportException>> transportFailures = new ConcurrentHashMap<>();
    private final Map<String, String> failedCommands = new ConcurrentHashMap<>();
    private final Map<String, Boolean> hanging = new ConcurrentHashMap<>();
    final List<Call> calls = new CopyOnWriteArrayList<>();

    FakeDynsecBrokers(String... brokerUrls) {
        for (var url : brokerUrls) {
            passwordsByBroker.put(url, new ConcurrentHashMap<>());
            rolesByBroker.put(url, new ConcurrentHashMap<>());
        }
    }

    void failTransport(String brokerUrl, Supplier<DynsecTransportException> failure) {
        transportFailures.put(brokerUrl, failure);
    }

    void stopFailingTransport(String brokerUrl) {
        transportFailures.remove(brokerUrl);
    }

    /** The broker answers this command with an error instead of applying it. */
    void failCommand(String brokerUrl, String command, String error) {
        failedCommands.put(brokerUrl + "|" + command, error);
    }

    void clearCommandFailures() {
        failedCommands.clear();
    }

    void hang(String brokerUrl) {
        hanging.put(brokerUrl, true);
    }

    boolean hasClient(String brokerUrl, String username) {
        return passwordsByBroker.get(brokerUrl).containsKey(username);
    }

    String roleOf(String brokerUrl, String username) {
        return rolesByBroker.get(brokerUrl).get(username);
    }

    String passwordOf(String brokerUrl, String username) {
        return passwordsByBroker.get(brokerUrl).get(username);
    }

    void seedClient(String brokerUrl, String username, String password) {
        passwordsByBroker.get(brokerUrl).put(username, password);
        rolesByBroker.get(brokerUrl).put(username, "sensor");
    }

    @Override
    public List<DynsecResult> execute(String brokerUrl, List<DynsecCommand> commands) throws DynsecTransportException {
        calls.add(new Call(brokerUrl, commands));
        if (hanging.containsKey(brokerUrl)) {
            try {
                Thread.sleep(60_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new DynsecTransportException("interrupted");
            }
        }
        var failure = transportFailures.get(brokerUrl);
        if (failure != null) throw failure.get();

        var passwords = passwordsByBroker.get(brokerUrl);
        var roles = rolesByBroker.get(brokerUrl);
        var results = new ArrayList<DynsecResult>();
        for (var command : commands) {
            var username = (String) command.arguments().get("username");
            var forced = failedCommands.get(brokerUrl + "|" + command.name());
            if (forced != null) {
                results.add(new DynsecResult(command.name(), forced));
                continue;
            }
            switch (command.name()) {
                case "deleteClient" -> {
                    if (passwords.remove(username) == null) {
                        results.add(new DynsecResult(command.name(), DynsecResult.CLIENT_NOT_FOUND));
                    } else {
                        roles.remove(username);
                        results.add(new DynsecResult(command.name(), null));
                    }
                }
                case "createClient" -> {
                    if (passwords.containsKey(username)) {
                        results.add(new DynsecResult(command.name(), "Client already exists"));
                    } else {
                        passwords.put(username, (String) command.arguments().get("password"));
                        results.add(new DynsecResult(command.name(), null));
                    }
                }
                case "addClientRole" -> {
                    if (!passwords.containsKey(username)) {
                        results.add(new DynsecResult(command.name(), DynsecResult.CLIENT_NOT_FOUND));
                    } else {
                        roles.put(username, (String) command.arguments().get("rolename"));
                        results.add(new DynsecResult(command.name(), null));
                    }
                }
                default -> results.add(new DynsecResult(command.name(), "Invalid/unknown command"));
            }
        }
        return results;
    }
}
