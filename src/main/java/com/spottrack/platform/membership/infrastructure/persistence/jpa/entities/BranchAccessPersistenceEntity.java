package com.spottrack.platform.membership.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.membership.domain.model.valueobjects.BranchAccessStatus;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "branch_accesses")
public class BranchAccessPersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "branch_access_id", nullable = false, unique = true)
    private String branchAccessId;

    @Column(name = "membership_id", nullable = false)
    private Long membershipId;

    @Column(name = "branch_id", nullable = false)
    private Long branchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BranchAccessStatus status;

    public BranchAccessPersistenceEntity() {}

    public String getBranchAccessId() { return branchAccessId; }
    public void setBranchAccessId(String branchAccessId) { this.branchAccessId = branchAccessId; }

    public Long getMembershipId() { return membershipId; }
    public void setMembershipId(Long membershipId) { this.membershipId = membershipId; }

    public Long getBranchId() { return branchId; }
    public void setBranchId(Long branchId) { this.branchId = branchId; }

    public BranchAccessStatus getStatus() { return status; }
    public void setStatus(BranchAccessStatus status) { this.status = status; }
}
