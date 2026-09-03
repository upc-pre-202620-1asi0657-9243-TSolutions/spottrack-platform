package com.spottrack.platform.profiles.domain.model.entities;

public class ClientGymAssociation {
    private Long id;
    private Long clientId;
    private String gymId;
    private boolean active;

    public ClientGymAssociation(Long clientId, String gymId, boolean active) {
        this.clientId = clientId;
        this.gymId = gymId;
        this.active = active;
    }

    public ClientGymAssociation(Long id, Long clientId, String gymId, boolean active) {
        this.id = id;
        this.clientId = clientId;
        this.gymId = gymId;
        this.active = active;
    }

    public Long getId() { return id; }
    public Long getClientId() { return clientId; }
    public String getGymId() { return gymId; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
