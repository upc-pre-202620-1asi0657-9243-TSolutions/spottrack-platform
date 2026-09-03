package com.spottrack.platform.profiles.domain.model.valueobjects;

public record AdminId (Long adminId){
    public AdminId{
        if (adminId == null || adminId < 1){
            throw new IllegalArgumentException("Admin id cannot be null or less than 1");
        }
    }
}
