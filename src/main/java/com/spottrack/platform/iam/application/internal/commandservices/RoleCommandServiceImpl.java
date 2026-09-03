package com.spottrack.platform.iam.application.internal.commandservices;

import com.spottrack.platform.iam.application.commandservices.RoleCommandService;
import com.spottrack.platform.iam.domain.model.commands.SeedRolesCommand;
import com.spottrack.platform.iam.domain.model.entities.Role;
import com.spottrack.platform.iam.domain.model.valueobjects.Roles;
import com.spottrack.platform.iam.domain.repositories.RoleRepository;
import org.springframework.stereotype.Service;

@Service
public class RoleCommandServiceImpl implements RoleCommandService {

    private final RoleRepository roleRepository;

    public RoleCommandServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void handle(SeedRolesCommand command) {
        for (Roles roleValue : Roles.values()) {
            if (!roleRepository.existsByName(roleValue)) {
                roleRepository.save(new Role(roleValue));
            }
        }
    }
}
