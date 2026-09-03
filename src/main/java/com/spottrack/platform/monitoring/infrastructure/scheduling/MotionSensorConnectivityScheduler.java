package com.spottrack.platform.monitoring.infrastructure.scheduling;

import com.spottrack.platform.monitoring.application.commandServices.MotionSensorCommandService;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorDisconnectedCommand;
import com.spottrack.platform.monitoring.domain.model.commands.MarkMotionSensorReconnectedCommand;
import com.spottrack.platform.monitoring.domain.repositories.MotionSensorRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates Edge IoT connectivity flakiness — there's no real hardware to report
 * a lost connection, so this randomly drops an online sensor's link, and restores
 * it on its own after a short while (per US21's "Escenario 2: Reconexión exitosa").
 */
@Service
public class MotionSensorConnectivityScheduler {

    private static final double DISCONNECT_PROBABILITY = 0.3;
    private static final long RECONNECT_AFTER_MINUTES = 1;

    private final MotionSensorRepository motionSensorRepository;
    private final MotionSensorCommandService motionSensorCommandService;

    public MotionSensorConnectivityScheduler(MotionSensorRepository motionSensorRepository,
                                              MotionSensorCommandService motionSensorCommandService) {
        this.motionSensorRepository = motionSensorRepository;
        this.motionSensorCommandService = motionSensorCommandService;
    }

    @Scheduled(fixedRate = 30000)
    public void simulateConnectivity() {
        motionSensorRepository.findAllOnline().forEach(sensor -> {
            if (ThreadLocalRandom.current().nextDouble() < DISCONNECT_PROBABILITY) {
                motionSensorCommandService.handle(new MarkMotionSensorDisconnectedCommand(sensor.getId()));
            }
        });

        var reconnectThreshold = LocalDateTime.now().minusMinutes(RECONNECT_AFTER_MINUTES);
        motionSensorRepository.findAllOfflineSince(reconnectThreshold).forEach(sensor ->
                motionSensorCommandService.handle(new MarkMotionSensorReconnectedCommand(sensor.getId())));
    }
}
