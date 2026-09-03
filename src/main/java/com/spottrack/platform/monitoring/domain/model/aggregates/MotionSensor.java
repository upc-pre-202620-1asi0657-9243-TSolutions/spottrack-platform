package com.spottrack.platform.monitoring.domain.model.aggregates;

import com.spottrack.platform.monitoring.domain.model.commands.RegisterMotionSensorCommand;
import com.spottrack.platform.monitoring.domain.model.events.MotionSensorDisconnectedEvent;
import com.spottrack.platform.monitoring.domain.model.events.MotionSensorReconnectedEvent;
import com.spottrack.platform.monitoring.domain.model.events.MotionSensorRegisteredEvent;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.MotionSensorId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MotionSensor extends AbstractDomainAggregateRoot<MotionSensor> {
    private Long id;
    private MotionSensorId motionSensorId;
    private EquipmentId equipmentId;
    private LocalDateTime registeredAt;
    private boolean online;
    private LocalDateTime lastStatusChangeAt;

    public MotionSensor(Long id, MotionSensorId motionSensorId, EquipmentId equipmentId, LocalDateTime registeredAt,
                         boolean online, LocalDateTime lastStatusChangeAt) {
        this.id = id;
        this.motionSensorId = motionSensorId;
        this.equipmentId = equipmentId;
        this.registeredAt = registeredAt;
        this.online = online;
        this.lastStatusChangeAt = lastStatusChangeAt;
    }

    public MotionSensor(RegisterMotionSensorCommand command) {
        this.id = null;
        this.motionSensorId = new MotionSensorId();
        this.equipmentId = new EquipmentId(command.equipmentId());
        this.registeredAt = LocalDateTime.now();
        this.online = true;
        this.lastStatusChangeAt = LocalDateTime.now();
    }

    public void onCreated() {
        registerDomainEvent(new MotionSensorRegisteredEvent(this.motionSensorId, this.equipmentId));
    }

    /**
     * Simulates a network disconnection (no real Edge hardware to report this).
     */
    public void markDisconnected() {
        this.online = false;
        this.lastStatusChangeAt = LocalDateTime.now();
        registerDomainEvent(new MotionSensorDisconnectedEvent(this.motionSensorId, this.equipmentId));
    }

    /**
     * Simulates the network reconnecting on its own, per US21's Escenario 2.
     */
    public void markReconnected() {
        this.online = true;
        this.lastStatusChangeAt = LocalDateTime.now();
        registerDomainEvent(new MotionSensorReconnectedEvent(this.motionSensorId, this.equipmentId));
    }
}
