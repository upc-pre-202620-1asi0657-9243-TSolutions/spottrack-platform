package com.spottrack.platform.maintenance.infrastructure.persistence.jpa.assemblers;

import com.spottrack.platform.maintenance.domain.model.aggregates.TechnicalTicket;
import com.spottrack.platform.maintenance.infrastructure.persistence.jpa.entities.TechnicalTicketPersistenceEntity;

import java.time.ZoneId;
import java.util.Date;

public class TechnicalTicketPersistenceAssembler {

    public static TechnicalTicket toDomainFromPersistence(TechnicalTicketPersistenceEntity entity) {
        return new TechnicalTicket(
                entity.getTicketId(),
                entity.getMaintenanceId(),
                entity.getEquipmentId(),
                entity.getTechnicianId(),
                entity.getDescription(),
                entity.getPriority(),
                entity.getType(),
                entity.getTicketStatus(),
                entity.getMaintenanceStatus(),
                entity.getCreatedAt() != null
                        ? entity.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                        : null
        );
    }

    public static TechnicalTicketPersistenceEntity toPersistenceFromDomain(TechnicalTicket ticket) {
        var entity = new TechnicalTicketPersistenceEntity();
        if (ticket.getCreatedAt() != null) {
            entity.setCreatedAt(Date.from(ticket.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()));
        }
        entity.setTicketId(ticket.getTicketId().uuid());
        entity.setMaintenanceId(ticket.getMaintenanceId());
        entity.setEquipmentId(ticket.getEquipmentId());
        entity.setTechnicianId(ticket.getTechnicianId());
        entity.setDescription(ticket.getDescription());
        entity.setPriority(ticket.getPriority());
        entity.setType(ticket.getType());
        entity.setTicketStatus(ticket.getTicketStatus().name());
        entity.setMaintenanceStatus(ticket.getMaintenanceStatus().name());
        return entity;
    }
}
