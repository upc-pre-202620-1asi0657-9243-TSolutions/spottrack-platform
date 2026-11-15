package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * QA-01 latency benchmark: publishes N compact-emulator motion signals and measures, from the moment the device
 * publishes until the dato is visible, (a) the equipment list the floor map reads, polled until the equipment is
 * OCCUPIED, and (b) the STOMP message on /topic/monitoring/session-trackers/{id}. Disabled unless {@code -Dqa01.run=true}.
 * <p>
 * Required settings ({@code -D} or env, see {@link Qa01Settings}): {@code qa01.mqtt.url}, {@code qa01.device.id},
 * {@code qa01.api.url}, {@code qa01.api.jwt} (admin), {@code qa01.gym.id}, {@code qa01.equipment.id}. Optional:
 * {@code qa01.mqtt.username|password|ca.file}, {@code qa01.runs} (50), {@code qa01.warmup} (3),
 * {@code qa01.poll.interval.ms} (20), {@code qa01.timeout.ms} (5000), {@code qa01.p95.threshold.ms} (2000),
 * {@code qa01.stomp} (true), {@code qa01.env.label}, {@code qa01.output.dir} (target/qa01).
 * <p>
 * The sensor behind {@code qa01.device.id} must be registered on {@code qa01.equipment.id} with
 * {@code payloadProfile=compact-emulator}. Every iteration first resets the equipment to AVAILABLE through
 * PATCH /equipments/{id}/status, so the AVAILABLE to OCCUPIED transition happens again.
 * <p>
 * Measurement error: t0 is taken right before the MQTT publish, t1 is the instant the poll response that shows
 * OCCUPIED is received. The real change happened somewhere inside the last poll cycle, so each HTTP figure can
 * overstate the latency by up to one poll interval plus one request round trip (both are reported). The STOMP
 * figure is stamped when the frame reaches the client and has no polling error.
 */
@EnabledIfSystemProperty(named = "qa01.run", matches = "true")
class Qa01LatencyBenchmark {

    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");

    private enum Outcome { OK, TIMEOUT, ERROR }

    private record Sample(int iteration, Outcome outcome, double httpMs, double stompMs, double pubAckMs, String detail) {
    }

    private record Config(String apiUrl, String jwt, String gymId, String equipmentId, int runs, int warmup,
                          long pollIntervalMs, long timeoutMs, long thresholdMs, boolean stomp, String label) {
    }

    @Test
    void measurePublishToVisibleLatency() throws Exception {
        var config = new Config(
                Qa01Settings.required("qa01.api.url"), Qa01Settings.required("qa01.api.jwt"),
                Qa01Settings.required("qa01.gym.id"), Qa01Settings.required("qa01.equipment.id"),
                (int) Qa01Settings.longOrDefault("qa01.runs", 50), (int) Qa01Settings.longOrDefault("qa01.warmup", 3),
                Qa01Settings.longOrDefault("qa01.poll.interval.ms", 20), Qa01Settings.longOrDefault("qa01.timeout.ms", 5000),
                Qa01Settings.longOrDefault("qa01.p95.threshold.ms", 2000), Qa01Settings.booleanOrDefault("qa01.stomp", true),
                Qa01Settings.orDefault("qa01.env.label", "unlabeled"));
        var brokerUrl = Qa01Settings.required("qa01.mqtt.url");
        var deviceId = Qa01Settings.required("qa01.device.id");

        var api = new Qa01ApiClient(config.apiUrl(), config.jwt());
        var pollCycles = new ArrayList<Double>();
        var samples = new ArrayList<Sample>();
        boolean stompAvailable;

        try (var emulator = new AlternativeSensorEmulator(brokerUrl, deviceId, Qa01Settings.mqttConnection())) {
            assertEquals("AVAILABLE", api.resetToAvailable(config.equipmentId()),
                    "PATCH /equipments/{id}/status did not leave the equipment AVAILABLE");
            assertEquals("AVAILABLE", api.equipmentStatus(config.gymId(), config.equipmentId()).orElse("not listed"),
                    "The equipment is not AVAILABLE in /gyms/{gymId}/equipments after the reset (check gym id, equipment id and ownership)");

            for (int w = 0; w < config.warmup(); w++) {
                api.resetToAvailable(config.equipmentId());
                emulator.publishMotion(1.0);
                var seen = pollUntilOccupied(api, config, System.nanoTime(), new ArrayList<>());
                if (seen < 0) {
                    fail("Warm-up signal never made the equipment OCCUPIED within %d ms: check that the sensor %s is registered on the equipment with payloadProfile=compact-emulator, paired, and that the backend reads this broker"
                            .formatted(config.timeoutMs(), deviceId));
                }
            }

            try (var stompProbe = config.stomp() ? openStompProbe(api, config) : null) {
                stompAvailable = stompProbe != null && verifyStomp(api, config, emulator, stompProbe);
                for (int i = 1; i <= config.runs(); i++) {
                    samples.add(measureOnce(i, api, config, emulator, stompAvailable ? stompProbe : null, pollCycles));
                }
            }
        }

        report(config, brokerUrl, samples, pollCycles, stompAvailable);
    }

    private Qa01StompProbe openStompProbe(Qa01ApiClient api, Config config) {
        try {
            var trackerId = api.activeTrackerId(config.equipmentId()).orElse(null);
            if (trackerId == null) {
                System.out.println("STOMP: no active session tracker found for the equipment, STOMP not measured");
                return null;
            }
            return Qa01StompProbe.connect(api.baseUrl(), config.jwt(), trackerId);
        } catch (Exception e) {
            System.out.println("STOMP: could not connect (" + e.getClass().getSimpleName() + ": " + e.getMessage() + "), STOMP not measured");
            return null;
        }
    }

    private boolean verifyStomp(Qa01ApiClient api, Config config, AlternativeSensorEmulator emulator, Qa01StompProbe probe)
            throws Exception {
        for (int attempt = 1; attempt <= 3; attempt++) {
            api.resetToAvailable(config.equipmentId());
            probe.drain();
            long t0 = System.nanoTime();
            emulator.publishMotion(1.0);
            if (probe.awaitArrival(t0, t0 + TimeUnit.MILLISECONDS.toNanos(config.timeoutMs())) >= 0) return true;
        }
        System.out.println("STOMP: subscribed but no message arrived for 3 probe signals, STOMP not measured");
        return false;
    }

    private Sample measureOnce(int iteration, Qa01ApiClient api, Config config, AlternativeSensorEmulator emulator,
                               Qa01StompProbe stompProbe, List<Double> pollCycles) throws InterruptedException {
        try {
            var reset = api.resetToAvailable(config.equipmentId());
            if (!"AVAILABLE".equals(reset)) {
                return new Sample(iteration, Outcome.ERROR, Double.NaN, Double.NaN, Double.NaN, "reset left status " + reset);
            }
            if (stompProbe != null) stompProbe.drain();

            long t0 = System.nanoTime();
            emulator.publishMotion(1.0);
            double pubAckMs = toMs(System.nanoTime() - t0);

            long httpNanos = pollUntilOccupied(api, config, t0, pollCycles);
            double stompMs = Double.NaN;
            boolean stompTimedOut = false;
            if (stompProbe != null) {
                long arrival = stompProbe.awaitArrival(t0, t0 + TimeUnit.MILLISECONDS.toNanos(config.timeoutMs()));
                if (arrival < 0) stompTimedOut = true; else stompMs = toMs(arrival - t0);
            }
            if (httpNanos < 0) {
                return new Sample(iteration, Outcome.TIMEOUT, Double.NaN, stompMs, pubAckMs, "equipment never OCCUPIED");
            }
            if (stompTimedOut) {
                return new Sample(iteration, Outcome.TIMEOUT, toMs(httpNanos), Double.NaN, pubAckMs, "no STOMP message");
            }
            return new Sample(iteration, Outcome.OK, toMs(httpNanos), stompMs, pubAckMs, "");
        } catch (IOException | org.eclipse.paho.client.mqttv3.MqttException e) {
            return new Sample(iteration, Outcome.ERROR, Double.NaN, Double.NaN, Double.NaN,
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Polls until the equipment is OCCUPIED; returns nanos since t0 at the response that showed it, or -1 on timeout. */
    private long pollUntilOccupied(Qa01ApiClient api, Config config, long t0, List<Double> pollCycles)
            throws IOException, InterruptedException {
        long deadline = t0 + TimeUnit.MILLISECONDS.toNanos(config.timeoutMs());
        while (System.nanoTime() < deadline) {
            long start = System.nanoTime();
            var status = api.equipmentStatus(config.gymId(), config.equipmentId()).orElse("not listed");
            long end = System.nanoTime();
            pollCycles.add(toMs(end - start));
            if ("OCCUPIED".equals(status)) return end - t0;
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(config.pollIntervalMs()));
        }
        return -1;
    }

    private void report(Config config, String brokerUrl, List<Sample> samples, List<Double> pollCycles, boolean stompAvailable)
            throws IOException {
        var out = new StringBuilder();
        var started = Instant.now();
        var host = URI.create(config.apiUrl()).getHost();
        var local = host == null || LOCAL_HOSTS.contains(host);

        out.append("=== QA-01 latency: device publish -> data visible ===\n");
        out.append("date (UTC)        : ").append(started).append('\n');
        out.append("environment label : ").append(config.label()).append('\n');
        out.append("commit            : ").append(gitCommit()).append('\n');
        out.append("jvm / os          : ").append(System.getProperty("java.version")).append(" / ")
                .append(System.getProperty("os.name")).append(' ').append(System.getProperty("os.arch")).append('\n');
        out.append("mqtt / api scheme : ").append(scheme(brokerUrl)).append(" / ").append(scheme(config.apiUrl())).append(" (hosts not recorded)\n");
        out.append("profile / signal  : compact-emulator, QoS 1, motion\n");
        out.append("runs / warm-up    : ").append(config.runs()).append(" / ").append(config.warmup()).append('\n');
        out.append("poll interval     : ").append(config.pollIntervalMs()).append(" ms, timeout ").append(config.timeoutMs()).append(" ms\n");
        if (local) {
            out.append("*** LOCAL RUN: lower bound only (no network, TLS, load balancer or Cloud SQL). NOT evidence for QA-01. ***\n");
        }

        var ok = samples.stream().filter(s -> s.outcome() == Outcome.OK).toList();
        var timeouts = samples.stream().filter(s -> s.outcome() == Outcome.TIMEOUT).count();
        var errors = samples.stream().filter(s -> s.outcome() == Outcome.ERROR).count();
        out.append("\nsignals           : ").append(samples.size()).append(" sent, ").append(ok.size()).append(" ok, ")
                .append(timeouts).append(" timeouts, ").append(errors).append(" errors\n\n");

        var httpVerdict = appendStats(out, "equipment list (HTTP poll)", ok.stream().map(Sample::httpMs).toList(), config, timeouts, errors);
        var stompVerdict = "n/a";
        if (stompAvailable) {
            stompVerdict = appendStats(out, "STOMP /topic/monitoring/session-trackers/{id}", ok.stream().map(Sample::stompMs).toList(), config, timeouts, errors);
        } else {
            out.append("STOMP             : not measured\n");
        }
        if (!ok.isEmpty()) {
            var pubAck = LatencyStats.of(ok.stream().map(Sample::pubAckMs).toList());
            out.append(String.format(Locale.ROOT, "publish -> PUBACK  : median %.1f ms, max %.1f ms (included in both figures)\n", pubAck.median(), pubAck.max()));
        }
        if (!pollCycles.isEmpty()) {
            var poll = LatencyStats.of(pollCycles);
            out.append(String.format(Locale.ROOT, "HTTP poll request : median round trip %.1f ms; measurement error of each HTTP figure <= %d ms interval + that round trip\n",
                    poll.median(), config.pollIntervalMs()));
        }
        out.append("percentile method : median = middle value (mean of the two middle for even n); p95 = nearest rank\n");
        out.append(String.format(Locale.ROOT, "\nVERDICT p95 < %d ms and no timeouts/errors: HTTP %s, STOMP %s\n", config.thresholdMs(), httpVerdict, stompVerdict));

        System.out.println(out);
        writeFiles(started, samples, out.toString());
    }

    private String appendStats(StringBuilder out, String name, List<Double> values, Config config, long timeouts, long errors) {
        if (values.isEmpty()) {
            out.append(name).append(": no successful sample\n\n");
            return "FAIL";
        }
        var stats = LatencyStats.of(values);
        out.append(name).append('\n');
        out.append(String.format(Locale.ROOT, "  n=%d  min=%.1f ms  median=%.1f ms  p95=%.1f ms  max=%.1f ms%n%n",
                stats.count(), stats.min(), stats.median(), stats.p95(), stats.max()));
        return stats.p95() < config.thresholdMs() && timeouts == 0 && errors == 0 ? "PASS" : "FAIL";
    }

    private void writeFiles(Instant started, List<Sample> samples, String summary) throws IOException {
        var dir = Path.of(Qa01Settings.orDefault("qa01.output.dir", "target/qa01"));
        Files.createDirectories(dir);
        var stamp = started.toString().replace(":", "-");
        var csv = new StringBuilder("iteration,outcome,http_ms,stomp_ms,puback_ms,detail\n");
        for (var s : samples) {
            csv.append(String.format(Locale.ROOT, "%d,%s,%s,%s,%s,%s%n", s.iteration(), s.outcome(), number(s.httpMs()),
                    number(s.stompMs()), number(s.pubAckMs()), s.detail().replace(',', ';')));
        }
        Files.writeString(dir.resolve("qa01-latency-" + stamp + ".csv"), csv);
        Files.writeString(dir.resolve("qa01-latency-" + stamp + ".txt"), summary);
        System.out.println("Raw samples and summary written to " + dir.toAbsolutePath());
    }

    private static String number(double value) {
        return Double.isNaN(value) ? "" : String.format(Locale.ROOT, "%.3f", value);
    }

    private static double toMs(long nanos) {
        return nanos / 1_000_000.0;
    }

    private static String scheme(String url) {
        var index = url.indexOf("://");
        return index < 0 ? "?" : url.substring(0, index);
    }

    private static String gitCommit() {
        try {
            var process = new ProcessBuilder("git", "rev-parse", "--short", "HEAD").redirectErrorStream(true).start();
            var output = new String(process.getInputStream().readAllBytes()).trim();
            return process.waitFor() == 0 && !output.isEmpty() ? output : "unknown";
        } catch (IOException | InterruptedException e) {
            return "unknown";
        }
    }
}
