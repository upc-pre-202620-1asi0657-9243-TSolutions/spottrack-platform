package com.spottrack.platform.gym.domain.model.entities;

import com.spottrack.platform.gym.domain.model.valueobjects.BranchId;
import com.spottrack.platform.gym.domain.model.valueobjects.EquipmentId;
import com.spottrack.platform.gym.domain.model.valueobjects.ZoneId;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Zone {
    private final ZoneId id;


    private String name;
    private int maximumOccupancy;

    private BranchId branchId;

    private List<EquipmentId> equipmentList;

    protected Zone() {
        this.id = null;
    }

    public Zone(String name, int maximumOccupancy, BranchId branchId) {
        this.id = new ZoneId(UUID.randomUUID().toString());
        this.name = name;
        this.maximumOccupancy = maximumOccupancy;
        this.equipmentList = new ArrayList<EquipmentId>();
        this.branchId = branchId;
    }

    public Zone(String zoneId, String name, int maximumOccupancy, BranchId branchId) {
        this.id = new ZoneId(zoneId);
        this.name = name;
        this.maximumOccupancy = maximumOccupancy;
        this.equipmentList = new ArrayList<EquipmentId>();
        this.branchId = branchId;
    }

}
