package com.spottrack.platform.monitoring.interfaces.rest.transform;

import com.spottrack.platform.monitoring.application.ports.QrCodeRenderer;
import com.spottrack.platform.monitoring.domain.model.valueobjects.IssuedSensorPairing;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SensorPairingQrPayloadResource;
import com.spottrack.platform.monitoring.interfaces.rest.resources.SensorPairingResource;
import tools.jackson.databind.json.JsonMapper;

import java.util.Base64;

public final class SensorPairingResourceFromIssued {
    public static final int QR_SIZE_PX = 320;

    private SensorPairingResourceFromIssued() {
    }

    public static SensorPairingResource toResourceFromIssued(IssuedSensorPairing issued, JsonMapper jsonMapper,
                                                            QrCodeRenderer qrCodeRenderer) {
        var qr = issued.qrPayload();
        var payload = new SensorPairingQrPayloadResource(qr.v(), qr.deviceId(), qr.secret(), qr.host(), qr.port(),
                qr.caSha256(), qr.exp(), qr.profile());
        var payloadJson = jsonMapper.writeValueAsString(payload);
        var png = qrCodeRenderer.renderPng(payloadJson, QR_SIZE_PX);
        return new SensorPairingResource(qr.deviceId(), issued.expiresAt(), payload, payloadJson,
                Base64.getEncoder().encodeToString(png));
    }
}
