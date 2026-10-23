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
    private LocalDateTime lastSeenAt;

    public MotionSensor(Long id, MotionSensorId motionSensorId, EquipmentId equipmentId, LocalDateTime registeredAt,
                         boolean online, LocalDateTime lastStatusChangeAt, LocalDateTime lastSeenAt) {
        this.id = id;
        this.motionSensorId = motionSensorId;
        this.equipmentId = equipmentId;
        this.registeredAt = registeredAt;
        this.online = online;
        this.lastStatusChangeAt = lastStatusChangeAt;
        this.lastSeenAt = lastSeenAt;
    }

    /**
     * A freshly registered sensor starts offline: it is only considered online
     * once its device sends the first heartbeat through the MQTT brokers.
     */
    public MotionSensor(RegisterMotionSensorCommand command) {
        this.id = null;
        this.motionSensorId = new MotionSensorId();
        this.equipmentId = new EquipmentId(command.equipmentId());
        this.registeredAt = LocalDateTime.now();
        this.online = false;
        this.lastStatusChangeAt = LocalDateTime.now();
        this.lastSeenAt = null;
    }

    public void onCreated() {
        registerDomainEvent(new MotionSensorRegisteredEvent(this.motionSensorId, this.equipmentId));
    }

    /**
     * Records a sign of life from the device (heartbeat or motion signal).
     * If the sensor was offline, this brings it back online (US21's "Escenario 2: Reconexión exitosa").
     */
    public void recordHeartbeat(LocalDateTime seenAt) {
        this.lastSeenAt = seenAt;
        if (!this.online) {
            markReconnected();
        }
    }

    /**
     * True when the device has not reported within the allowed window, i.e. it
     * should be treated as disconnected.
     */
    public boolean isHeartbeatOverdue(LocalDateTime threshold) {
        return lastSeenAt == null || lastSeenAt.isBefore(threshold);
    }

    public void markDisconnected() {
        this.online = false;
        this.lastStatusChangeAt = LocalDateTime.now();
        registerDomainEvent(new MotionSensorDisconnectedEvent(this.motionSensorId, this.equipmentId));
    }

    public void markReconnected() {
        this.online = true;
        this.lastStatusChangeAt = LocalDateTime.now();
        registerDomainEvent(new MotionSensorReconnectedEvent(this.motionSensorId, this.equipmentId));
    }
}
