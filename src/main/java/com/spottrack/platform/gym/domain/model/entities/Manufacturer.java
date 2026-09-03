package com.spottrack.platform.gym.domain.model.entities;

import com.spottrack.platform.gym.domain.model.valueobjects.ManufacturerId;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

import java.util.UUID;

@Getter
@Entity
public class Manufacturer {

    @EmbeddedId
    private final ManufacturerId id;


    private String name;
    private String country;
    private String website;

    /**
     * Class is still missing the contactEmail, this one has to be a valueobject in shared-kernel
     */

    public Manufacturer(String name, String country, String website){
        this.name=name;
        this.country=country;
        this.website=website;
        this.id = new ManufacturerId( UUID.randomUUID().toString());
    }


}
