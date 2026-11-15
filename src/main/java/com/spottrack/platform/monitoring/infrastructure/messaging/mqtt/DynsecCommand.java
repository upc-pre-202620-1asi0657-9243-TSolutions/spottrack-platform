package com.spottrack.platform.monitoring.infrastructure.messaging.mqtt;

import java.util.LinkedHashMap;
import java.util.Map;

/** One command of the Mosquitto dynamic-security control topic. Its arguments may hold a secret, so it never prints them. */
record DynsecCommand(String name, Map<String, Object> arguments) {

    DynsecCommand {
        arguments = Map.copyOf(arguments);
    }

    static DynsecCommand deleteClient(String username) {
        return new DynsecCommand("deleteClient", Map.of("username", username));
    }

    static DynsecCommand createClient(String username, String password) {
        return new DynsecCommand("createClient", Map.of("username", username, "password", password));
    }

    static DynsecCommand addClientRole(String username, String role) {
        var arguments = new LinkedHashMap<String, Object>();
        arguments.put("username", username);
        arguments.put("rolename", role);
        return new DynsecCommand("addClientRole", arguments);
    }

    @Override
    public String toString() {
        return "DynsecCommand[" + name + "]";
    }
}
