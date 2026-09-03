package com.spottrack.platform.gym.domain.model.entities;

import com.spottrack.platform.gym.domain.model.valueobjects.Dni;

public class GymWhitelistEntry {
    private Long id;
    private String gymId;
    private Dni dni;

    public GymWhitelistEntry(String gymId, Dni dni) {
        this.gymId = gymId;
        this.dni = dni;
    }

    public GymWhitelistEntry(Long id, String gymId, Dni dni) {
        this.id = id;
        this.gymId = gymId;
        this.dni = dni;
    }

    public Long getId() { return id; }
    public String getGymId() { return gymId; }
    public Dni getDni() { return dni; }
}
