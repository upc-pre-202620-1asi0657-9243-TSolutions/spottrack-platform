package com.spottrack.platform.gym.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
    name = "gym_whitelist",
    uniqueConstraints = @UniqueConstraint(columnNames = {"gym_id", "dni"})
)
public class GymWhitelistPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "gym_id", nullable = false)
    private String gymId;

    @Column(nullable = false)
    private String dni;

    public GymWhitelistPersistenceEntity(String gymId, String dni) {
        this.gymId = gymId;
        this.dni = dni;
    }
}
