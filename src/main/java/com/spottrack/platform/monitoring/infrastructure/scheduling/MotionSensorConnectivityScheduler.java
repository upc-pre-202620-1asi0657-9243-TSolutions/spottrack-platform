package com.spottrack.platform.monitoring.infrastructure.scheduling;

import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorDisconnectedCommand;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Detects disconnected sensors from missing heartbeats (US21).
 * <p>
 * Every device publishes {@code sensors/{deviceId}/heartbeat} periodically. A sensor
 * that has not reported within {@code monitoring.heartbeat.timeout-seconds} is marked
 * offline; the next heartbeat that arrives brings it back online
 * (see {@code MotionSensor#recordHeartbeat}).
 */
@Service
public class MotionSensorConnectivityScheduler {

    private final MotionSensorRepository motionSensorRepository;
    private final MotionSensorCommandService motionSensorCommandService;
    private final long heartbeatTimeoutSeconds;

    public MotionSensorConnectivityScheduler(MotionSensorRepository motionSensorRepository,
                                              MotionSensorCommandService motionSensorCommandService,
                                              @Value("${monitoring.heartbeat.timeout-seconds:30}") long heartbeatTimeoutSeconds) {
        this.motionSensorRepository = motionSensorRepository;
        this.motionSensorCommandService = motionSensorCommandService;
        this.heartbeatTimeoutSeconds = heartbeatTimeoutSeconds;
    }

    @Scheduled(fixedRateString = "${monitoring.heartbeat.check-interval-ms:10000}")
    public void markSilentSensorsDisconnected() {
        var threshold = LocalDateTime.now().minusSeconds(heartbeatTimeoutSeconds);
        motionSensorRepository.findAllOnline().stream()
                .filter(sensor -> sensor.isHeartbeatOverdue(threshold))
                .forEach(sensor -> motionSensorCommandService.handle(
                        new MarkMotionSensorDisconnectedCommand(sensor.getId())));
    }
}
