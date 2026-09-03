package com.spottrack.platform.gym.infrastructure.scheduling;

import com.spottrack.platform.gym.application.commandServices.EquipmentCommandService;
import com.spottrack.platform.gym.domain.model.commands.MarkMaintenanceThresholdReached;
import com.spottrack.platform.gym.domain.repositories.EquipmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Policy: [Maintenance Threshold Defined] Threshold reached? → mark reached.
 *
 * Hourly poll for equipment whose maintenanceThreshold date has passed and
 * hasn't already triggered an alert. Marking it reached fans out (via
 * EquipmentMaintenanceThresholdReachedEventHandler) into an automatic
 * Maintenance request and a separate alert notification.
 */
@Service
@Slf4j
public class EquipmentMaintenanceThresholdScheduler {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentCommandService equipmentCommandService;

    public EquipmentMaintenanceThresholdScheduler(EquipmentRepository equipmentRepository,
                                                   EquipmentCommandService equipmentCommandService) {
        this.equipmentRepository = equipmentRepository;
        this.equipmentCommandService = equipmentCommandService;
    }

    @Scheduled(fixedRate = 3600000)
    public void checkMaintenanceThresholds() {
        var overdue = equipmentRepository.findByMaintenanceThresholdLessThanEqualAndMaintenanceAlertSentFalse(LocalDate.now());
        log.info("[MaintenanceThresholdScheduler] Checked at {}, found {} equipment(s) overdue.", LocalDate.now(), overdue.size());
        overdue.forEach(equipment -> {
            var result = equipmentCommandService.handle(new MarkMaintenanceThresholdReached(equipment.getId()));
            log.info("[MaintenanceThresholdScheduler] Marked equipmentId={} as reached, result={}", equipment.getId().uuid(), result);
        });
    }
}
