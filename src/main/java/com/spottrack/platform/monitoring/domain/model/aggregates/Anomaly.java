package com.spottrack.platform.monitoring.domain.model.aggregates;

import com.spottrack.platform.monitoring.domain.model.commands.ReportAnomalyCommand;
import com.spottrack.platform.monitoring.domain.model.events.EquipmentAnomalyReportSubmitted;
import com.spottrack.platform.monitoring.domain.model.valueobjects.AnomalyId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ReservationId;
import com.spottrack.platform.monitoring.domain.model.valueobjects.ZoneId;
import com.spottrack.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class Anomaly extends AbstractDomainAggregateRoot<Anomaly> {
    private Long id;
    private AnomalyId anomalyId;
    private ReservationId reservationId;
    private LocalDate emissionDate;
    private EquipmentId equipmentId;
    private ZoneId zoneId;
    private String anomalyDescription;


    public Anomaly(Long id, AnomalyId anomalyId, ReservationId reservationId, EquipmentId equipmentId, ZoneId zoneId, String anomalyDescription, LocalDate emissionDate) {
        this.id = id;
        this.anomalyId = anomalyId;
        this.reservationId = reservationId;
        this.equipmentId = equipmentId;
        this.zoneId = zoneId;
        this.anomalyDescription = anomalyDescription;
        this.emissionDate = emissionDate;
    }

    public Anomaly(ReportAnomalyCommand command){
        this.id = null;
        this.anomalyId = new AnomalyId();
        this.reservationId = new ReservationId(command.reservationId());
        this.equipmentId = new EquipmentId(command.equipmentId());
        this.zoneId = new ZoneId(command.zoneId());
        this.anomalyDescription = command.anomalyDescription();
        this.emissionDate = LocalDate.now();
    }


    public void onCreated() {
        registerDomainEvent(EquipmentAnomalyReportSubmitted.from(this));
    }
}
