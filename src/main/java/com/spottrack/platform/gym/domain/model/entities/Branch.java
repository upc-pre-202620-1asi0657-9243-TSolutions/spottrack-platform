package com.spottrack.platform.gym.domain.model.entities;

import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Branch {

    private final BranchId id;
    private String gymId;
    private String name;
    private String address;

    private List<Zone> zoneList;

    protected Branch() {
        this.id = null;
    }

    public Branch(String gymId, String name, String address) {
        this.id = new BranchId(UUID.randomUUID().toString());
        this.gymId = gymId;
        this.name = name;
        this.address = address;
        this.zoneList = new ArrayList<Zone>();
    }

    public Branch(String branchId, String gymId, String name, String address) {
        this.id = new BranchId(branchId);
        this.gymId = gymId;
        this.name = name;
        this.address = address;
        this.zoneList = new ArrayList<Zone>();
    }
}
