package com.spottrack.platform.iam.application.commandservices;

import com.spottrack.platform.iam.domain.model.commands.SeedRolesCommand;

public interface RoleCommandService {
    void handle(SeedRolesCommand command);
}
