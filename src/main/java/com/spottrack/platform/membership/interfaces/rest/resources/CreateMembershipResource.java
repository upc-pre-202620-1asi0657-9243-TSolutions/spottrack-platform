package com.spottrack.platform.membership.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateMembershipResource(
        @NotBlank String membershipTier,
        @NotNull @Positive BigDecimal priceAmount,
        @NotBlank String priceCurrency,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}
