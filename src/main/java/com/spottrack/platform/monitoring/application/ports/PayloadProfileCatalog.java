package com.spottrack.platform.monitoring.application.ports;

public interface PayloadProfileCatalog {

    /** True when sensors can be configured with this payload profile; a null or blank profile means the default one. */
    boolean isSupported(String profile);
}
