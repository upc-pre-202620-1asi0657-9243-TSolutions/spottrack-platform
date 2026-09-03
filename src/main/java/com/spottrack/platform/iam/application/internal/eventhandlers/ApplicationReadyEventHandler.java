package com.spottrack.platform.iam.application.internal.eventhandlers;

import com.spottrack.platform.iam.application.commandservices.RoleCommandService;
import com.spottrack.platform.iam.domain.model.commands.SeedRolesCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ApplicationReadyEventHandler {

    private final RoleCommandService roleCommandService;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        log.info("Seeding roles...");
        roleCommandService.handle(new SeedRolesCommand());
        log.info("Roles seeded successfully.");
    }
}
