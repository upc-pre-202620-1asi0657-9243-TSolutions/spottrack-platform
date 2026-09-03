package com.spottrack.platform.membership.application.queryservices;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.queries.GetPaymentByIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetPaymentsByUserIdQuery;

import java.util.List;
import java.util.Optional;

public interface PaymentQueryService {
    Optional<Payment> handle(GetPaymentByIdQuery query);
    List<Payment> handle(GetPaymentsByUserIdQuery query);
}
