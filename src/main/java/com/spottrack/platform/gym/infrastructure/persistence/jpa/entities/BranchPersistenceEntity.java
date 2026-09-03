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

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name="branches")

public class BranchPersistenceEntity extends AuditableAbstractPersistenceEntity {
    @Column(nullable = true, unique = true)
    private String branchId;

    @Column(nullable = true)
    private String gymId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @OneToMany
    List<ZonePersistenceEntity> zones;

}
