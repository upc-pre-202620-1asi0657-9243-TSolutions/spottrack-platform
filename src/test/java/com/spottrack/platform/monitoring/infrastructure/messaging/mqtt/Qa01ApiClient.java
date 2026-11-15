package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;

/** The slice of the backend REST API the QA-01 benchmark needs, authenticated with an admin JWT. */
final class Qa01ApiClient {

    private static final JsonMapper MAPPER = JsonMapper.builder().build();

    private final String baseUrl;
    private final String jwt;
    private final HttpClient http = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    Qa01ApiClient(String baseUrl, String jwt) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.jwt = jwt;
    }

    String baseUrl() {
        return baseUrl;
    }

    /** What the floor map reads: the equipment list of the gym. Returns the equipment's status, or empty when it is not listed. */
    Optional<String> equipmentStatus(String gymId, String equipmentId) throws IOException, InterruptedException {
        var body = send(request("/api/v1/gyms/" + gymId + "/equipments").GET().build());
        for (JsonNode equipment : MAPPER.readTree(body)) {
            if (equipmentId.equals(equipment.path("equipmentId").asString())) {
                return Optional.of(equipment.path("status").asString());
            }
        }
        return Optional.empty();
    }

    /** PATCH /equipments/{id}/status to AVAILABLE; returns the status the API answers with. */
    String resetToAvailable(String equipmentId) throws IOException, InterruptedException {
        var payload = MAPPER.writeValueAsString(Map.of("id", equipmentId, "status", "AVAILABLE"));
        var body = send(request("/api/v1/equipments/" + equipmentId + "/status")
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(payload))
                .build());
        return MAPPER.readTree(body).path("status").asString();
    }

    /** Id of the active session tracker of the equipment, the one its STOMP topic is named after. */
    Optional<String> activeTrackerId(String equipmentId) throws IOException, InterruptedException {
        var body = send(request("/api/v1/monitoring/session-trackers/me").GET().build());
        for (JsonNode tracker : MAPPER.readTree(body)) {
            if (equipmentId.equals(tracker.path("equipmentId").asString()) && tracker.path("sessionIsActive").asBoolean()) {
                return Optional.of(tracker.path("sessionTrackerId").asString());
            }
        }
        return Optional.empty();
    }

    private HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(Duration.ofSeconds(10))
                .header("Authorization", "Bearer " + jwt)
                .header("Accept", "application/json");
    }

    private String send(HttpRequest request) throws IOException, InterruptedException {
        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("%s %s answered HTTP %d".formatted(request.method(), request.uri().getPath(), response.statusCode()));
        }
        return response.body();
    }
}
