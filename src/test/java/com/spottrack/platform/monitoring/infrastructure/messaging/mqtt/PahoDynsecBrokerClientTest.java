package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PahoDynsecBrokerClientTest {
    private static final String ID = "req-1";

    @Test
    void requestCarriesOneCommandPerEntryWithItsCorrelationData() {
        var json = JsonMapper.builder().build().readTree(PahoDynsecBrokerClient.buildRequest(ID, List.of(
                DynsecCommand.deleteClient("dev-1"),
                DynsecCommand.createClient("dev-1", "pw"),
                DynsecCommand.addClientRole("dev-1", "sensor"))));

        var commands = json.path("commands");
        assertEquals(3, commands.size());
        assertEquals("deleteClient", commands.get(0).path("command").asString());
        assertEquals("req-1:0", commands.get(0).path("correlationData").asString());
        assertEquals("createClient", commands.get(1).path("command").asString());
        assertEquals("pw", commands.get(1).path("password").asString());
        assertEquals("sensor", commands.get(2).path("rolename").asString());
        assertEquals("req-1:2", commands.get(2).path("correlationData").asString());
    }

    @Test
    void specialCharactersInTheSecretAreEscaped() {
        var secret = "a\"b\\c\n";
        var json = JsonMapper.builder().build().readTree(PahoDynsecBrokerClient.buildRequest(ID,
                List.of(DynsecCommand.createClient("dev-1", secret))));

        assertEquals(secret, json.path("commands").get(0).path("password").asString());
    }

    @Test
    void successAndBrokerErrorsAreParsedPerCommand() {
        var results = PahoDynsecBrokerClient.parseResponse(ID, """
                {"responses":[
                  {"command":"deleteClient","error":"Client not found","correlationData":"req-1:0"},
                  {"command":"createClient","correlationData":"req-1:1"},
                  {"command":"addClientRole","correlationData":"req-1:2"}]}""").orElseThrow();

        assertEquals(3, results.size());
        assertEquals(DynsecResult.CLIENT_NOT_FOUND, results.get(0).error());
        assertNull(results.get(1).error());
        assertTrue(results.get(2).ok());
        assertEquals("addClientRole", results.get(2).command());
    }

    @Test
    void answersToOtherRequestsAndGarbageAreIgnored() {
        assertTrue(PahoDynsecBrokerClient.parseResponse(ID,
                "{\"responses\":[{\"command\":\"createClient\",\"correlationData\":\"other:0\"}]}").isEmpty());
        assertTrue(PahoDynsecBrokerClient.parseResponse(ID, "{\"responses\":[{\"command\":\"createClient\"}]}").isEmpty());
        assertTrue(PahoDynsecBrokerClient.parseResponse(ID, "not json").isEmpty());
        assertTrue(PahoDynsecBrokerClient.parseResponse(ID, "{}").isEmpty());
    }

    @Test
    void unreachableBrokerIsATransportError() {
        var client = new PahoDynsecBrokerClient(new MqttConnectionSettings("admin", "pw", ""),
                Duration.ofSeconds(1), Duration.ofSeconds(1));

        // Nothing listens on port 1 of the loopback.
        assertThrows(DynsecTransportException.class,
                () -> client.execute("tcp://127.0.0.1:1", List.of(DynsecCommand.deleteClient("dev-1"))));
    }
}
