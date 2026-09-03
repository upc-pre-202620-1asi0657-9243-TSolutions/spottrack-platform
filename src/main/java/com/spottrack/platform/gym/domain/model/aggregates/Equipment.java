package com.spottrack.platform.gym.domain.model.aggregates;

import com.spottrack.platform.gym.domain.model.commands.RegisterEquipment;
import com.spottrack.platform.gym.domain.model.events.EquipmentStatusUpdatedEvent;
import com.spottrack.platform.gym.domain.model.events.MaintenanceThresholdDefinedEvent;
import com.spottrack.platform.gym.domain.model.events.MaintenanceThresholdReachedEvent;
import com.spottrack.platform.gym.domain.model.entities.Manufacturer;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentStatus;
import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.spottrack.platform.shared.domain.model.valueobjects.Money;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class Equipment extends AbstractDomainAggregateRoot<Equipment> {

    private Long persistenceId;
    private EquipmentId id;
    private EquipmentStatus status;

    private String equipmentName;
    private String model;
    private Money purchasePrice;

    private ManufacturerId manufacturerId;
    private LocalDate maintenanceThreshold;
    private boolean maintenanceAlertSent;
    private ZoneId zoneId;


    public Equipment() {}

    public Equipment(EquipmentStatus status, String equipmentName, String model, ManufacturerId manufacturerId, BigDecimal amount, String currency, ZoneId zoneId) {
        this.id = new EquipmentId(UUID.randomUUID().toString());
        this.status = status;
        this.equipmentName = equipmentName;
        this.model = model;
        this.manufacturerId = manufacturerId;
        this.purchasePrice = new Money(amount, currency);
        /**
         * Now is the default date, an Admin must define the date manually
         */
        this.maintenanceThreshold = LocalDate.now();
        this.zoneId = zoneId;
    }

    public Equipment(RegisterEquipment command) {
        this.id = new EquipmentId(UUID.randomUUID().toString());
        this.status = command.status();
        this.equipmentName = command.equipmentName();
        this.model = command.model();
        this.manufacturerId = command.manufacturerId();
        this.zoneId = command.zoneId();
        this.purchasePrice = command.purchasePrice();
        if (command.maintenanceThreshold() != null) {
            validateMaintenanceThreshold(command.maintenanceThreshold());
            this.maintenanceThreshold = command.maintenanceThreshold();
        } else {
            this.maintenanceThreshold = LocalDate.now();
        }
    }

    /**
     * A maintenance threshold is a future due date: it must not be in the past and
     * must not be set unrealistically far ahead. Applied only to user-supplied dates,
     * never to the assembler's reconstruction setter.
     */
    private void validateMaintenanceThreshold(LocalDate date) {
        if (date.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("gym.error.equipment.maintenanceThreshold.past");
        }
        if (date.isAfter(LocalDate.now().plusYears(5))) {
            throw new IllegalArgumentException("gym.error.equipment.maintenanceThreshold.tooFarInFuture");
        }
    }


    public void markEquipmentOutOfService(){
        if (this.status == EquipmentStatus.DECOMMISSIONED) {
            throw new IllegalStateException("gym.error.equipment.decommissionedIsTerminal");
        }
        this.status = EquipmentStatus.OUT_OF_SERVICE;
        registerDomainEvent(new EquipmentStatusUpdatedEvent(this.id.uuid(), this.status));
    }

    public void updateStatus(EquipmentStatus status){
        if (this.status == EquipmentStatus.DECOMMISSIONED) {
            throw new IllegalStateException("gym.error.equipment.decommissionedIsTerminal");
        }
        this.status = status;
        registerDomainEvent(new EquipmentStatusUpdatedEvent(this.id.uuid(), this.status));
    }

    public void relocateEquipment(ZoneId zoneId) {
        if (zoneId != null){
            this.zoneId = zoneId;
        } else {
            throw new IllegalArgumentException("zoneId must not be null");
        }
    }

    /**
     * Named distinctly from the Lombok-generated setMaintenanceThreshold(...) —
     * that plain setter is what the persistence assembler uses to reconstruct an
     * Equipment from a DB row, and must stay a no-side-effect field assignment.
     * This method is the actual DefineMaintenanceThreshold command behavior.
     */
    public void defineMaintenanceThreshold(LocalDate date){
        validateMaintenanceThreshold(date);
        this.maintenanceThreshold = date;
        // A newly-defined threshold hasn't been reached yet — clear the flag so the
        // threshold-reached policy can fire again against the new date.
        this.maintenanceAlertSent = false;
        registerDomainEvent(new MaintenanceThresholdDefinedEvent(this.id.uuid(), date));
    }

    /**
     * Called by EquipmentMaintenanceThresholdScheduler once maintenanceThreshold has passed.
     * maintenanceAlertSent guards against re-firing every scheduler tick.
     */
    public void markMaintenanceThresholdReached(){
        this.maintenanceAlertSent = true;
        registerDomainEvent(new MaintenanceThresholdReachedEvent(this.id.uuid(), this.maintenanceThreshold));
    }
}