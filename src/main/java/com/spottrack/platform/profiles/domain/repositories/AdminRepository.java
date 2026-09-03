package com.spottrack.platform.profiles.domain.repositories;

import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;

import java.util.Optional;

public interface AdminRepository {
    Optional<Admin> findById(AdminId adminId);

    Optional<Admin> findByUserId(Long userId);

    Optional<Admin> findByEmailAddress(EmailAddress emailAddress);

    Admin save(Admin admin);

    boolean existsByEmailAddress(EmailAddress emailAddress);
}
