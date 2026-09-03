package com.spottrack.platform.profiles.infrastructure.persistence.jpa.embeddables;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BusinessInfoPersistenceEmbeddable {

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "ruc", length = 11)
    private String ruc;

    @Column(name = "legal_structure")
    private String legalStructure;

    @Column(name = "company_phone")
    private String companyPhone;

    @Column(name = "company_email")
    private String companyEmail;

    @Column(name = "street_address")
    private String streetAddress;

    @Column(name = "city")
    private String city;

    @Column(name = "district")
    private String district;

    public BusinessInfoPersistenceEmbeddable() {
    }

    public BusinessInfoPersistenceEmbeddable(
            String companyName, String ruc, String legalStructure,
            String companyPhone, String companyEmail,
            String streetAddress, String city, String district) {
        this.companyName = companyName;
        this.ruc = ruc;
        this.legalStructure = legalStructure;
        this.companyPhone = companyPhone;
        this.companyEmail = companyEmail;
        this.streetAddress = streetAddress;
        this.city = city;
        this.district = district;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getRuc() { return ruc; }
    public void setRuc(String ruc) { this.ruc = ruc; }

    public String getLegalStructure() { return legalStructure; }
    public void setLegalStructure(String legalStructure) { this.legalStructure = legalStructure; }

    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
}
