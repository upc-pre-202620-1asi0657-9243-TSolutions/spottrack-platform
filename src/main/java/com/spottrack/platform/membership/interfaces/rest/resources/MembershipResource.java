package com.spottrack.platform.membership.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record MembershipResource(
        Long id,
        UUID membershipId,
        Long clientId,
        String membershipTier,
        BigDecimal priceAmount,
        String priceCurrency,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        boolean cancelAtPeriodEnd,
        String pendingDowngradeTier
) {}
