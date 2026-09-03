package com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities;

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
    name = "client_gym_associations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "gym_id"})
)
public class ClientGymAssociationPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "gym_id", nullable = false)
    private String gymId;

    @Column(nullable = false)
    private boolean active;

    public ClientGymAssociationPersistenceEntity(Long clientId, String gymId, boolean active) {
        this.clientId = clientId;
        this.gymId = gymId;
        this.active = active;
    }
}
