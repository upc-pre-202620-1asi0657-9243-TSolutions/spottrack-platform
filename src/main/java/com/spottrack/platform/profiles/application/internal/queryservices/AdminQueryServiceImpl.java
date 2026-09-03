package com.spottrack.platform.profiles.application.internal.queryservices;

import com.spottrack.platform.profiles.application.queryservices.AdminQueryService;
import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByEmailQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByIdQuery;
import com.spottrack.platform.profiles.domain.model.queries.GetAdminByUserIdQuery;
import com.spottrack.platform.profiles.domain.repositories.AdminRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AdminQueryServiceImpl implements AdminQueryService {
    private final AdminRepository adminRepository;

    public AdminQueryServiceImpl(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public Optional<Admin> handle(GetAdminByIdQuery query) {
        return adminRepository.findById(query.adminId());
    }

    @Override
    public Optional<Admin> handle(GetAdminByEmailQuery query) {
        return adminRepository.findByEmailAddress(query.emailAddress());
    }

    @Override
    public Optional<Admin> handle(GetAdminByUserIdQuery query) {
        return adminRepository.findByUserId(query.userId());
    }
}
