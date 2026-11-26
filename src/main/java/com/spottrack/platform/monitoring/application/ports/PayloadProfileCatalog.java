package com.spottrack.platform.monitoring.application.ports;

public interface PayloadProfileCatalog {

    /** Profile used by sensors that have none configured: SpotTrack's own payload format. */
    String DEFAULT_PROFILE = "default";

    /** True when sensors can be configured with this payload profile; a null or blank profile means the default one. */
    boolean isSupported(String profile);

    /** The profile a sensor configured with {@code profile} actually publishes with. */
    default String effectiveProfile(String profile) {
        return profile == null || profile.isBlank() ? DEFAULT_PROFILE : profile;
    }
}
