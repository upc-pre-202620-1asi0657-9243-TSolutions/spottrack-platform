package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record RenewMembershipResource(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
