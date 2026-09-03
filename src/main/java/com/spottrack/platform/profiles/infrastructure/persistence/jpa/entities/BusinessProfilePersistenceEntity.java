package com.spottrack.platform.profiles.infrastructure.persistence.jpa.entities;

import com.spottrack.platform.profiles.infrastructure.persistence.jpa.embeddables.BusinessInfoPersistenceEmbeddable;
import com.spottrack.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "business_profiles")
public class BusinessProfilePersistenceEntity extends AuditableAbstractPersistenceEntity {

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "companyName",   column = @Column(name = "company_name")),
            @AttributeOverride(name = "ruc",           column = @Column(name = "ruc", length = 11)),
            @AttributeOverride(name = "legalStructure",column = @Column(name = "legal_structure")),
            @AttributeOverride(name = "companyPhone",  column = @Column(name = "company_phone")),
            @AttributeOverride(name = "companyEmail",  column = @Column(name = "company_email")),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "street_address")),
            @AttributeOverride(name = "city",          column = @Column(name = "city")),
            @AttributeOverride(name = "district",      column = @Column(name = "district"))
    })
    private BusinessInfoPersistenceEmbeddable businessInfo;

    public BusinessProfilePersistenceEntity() {
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public BusinessInfoPersistenceEmbeddable getBusinessInfo() { return businessInfo; }
    public void setBusinessInfo(BusinessInfoPersistenceEmbeddable businessInfo) { this.businessInfo = businessInfo; }
}
