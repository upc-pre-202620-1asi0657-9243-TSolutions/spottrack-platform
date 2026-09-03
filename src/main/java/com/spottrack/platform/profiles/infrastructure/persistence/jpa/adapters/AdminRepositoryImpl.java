package com.spottrack.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.profiles.domain.model.aggregates.Admin;
import com.spottrack.platform.profiles.domain.model.valueobjects.AdminId;
import com.spottrack.platform.profiles.domain.model.valueobjects.EmailAddress;
import com.spottrack.platform.profiles.domain.repositories.AdminRepository;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.assemblers.AdminPersistenceAssembler;
import com.spottrack.platform.profiles.infrastructure.persistence.jpa.repositories.AdminPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminPersistenceRepository adminPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AdminRepositoryImpl(
            AdminPersistenceRepository adminPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.adminPersistenceRepository = adminPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Admin> findById(AdminId adminId) {
        return adminPersistenceRepository.findById(adminId.adminId())
                .map(AdminPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Admin> findByUserId(Long userId) {
        return adminPersistenceRepository.findByUserId(userId)
                .map(AdminPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Optional<Admin> findByEmailAddress(EmailAddress emailAddress) {
        return adminPersistenceRepository.findByEmailAddress(emailAddress)
                .map(AdminPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public Admin save(Admin admin) {
        boolean isNew = admin.getId() == null;
        var savedEntity = adminPersistenceRepository.save(AdminPersistenceAssembler.toPersistenceFromDomain(admin));
        var savedAdmin = AdminPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedAdmin.onCreated();
            savedAdmin.domainEvents().forEach(eventPublisher::publishEvent);
            savedAdmin.clearDomainEvents();
        }
        return savedAdmin;
    }

    @Override
    public boolean existsByEmailAddress(EmailAddress emailAddress) {
        return adminPersistenceRepository.countByEmailAddress(emailAddress) > 0;
    }
}
