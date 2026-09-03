package com.spottrack.platform.gym.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;


@Entity
@Table(name="zones")
@Getter
@Setter
@NoArgsConstructor
public class ZonePersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = false)
    private String zoneId;
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int maximumOccupancy;

    @Column(nullable = true)
    private String branchId;

    @OneToMany
    private List<EquipmentPersistenceEntity> equipmentList;
}
