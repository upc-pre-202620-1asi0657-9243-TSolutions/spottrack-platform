package com.spottrack.platform.membership.application.internal.queryservices;

import com.spottrack.platform.membership.application.queryservices.PaymentQueryService;
import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.queries.GetPaymentByIdQuery;
import com.spottrack.platform.membership.domain.model.queries.GetPaymentsByUserIdQuery;
import com.spottrack.platform.membership.domain.repositories.PaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentQueryServiceImpl implements PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public PaymentQueryServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Optional<Payment> handle(GetPaymentByIdQuery query) {
        return paymentRepository.findByPaymentId(query.paymentId());
    }

    @Override
    public List<Payment> handle(GetPaymentsByUserIdQuery query) {
        return paymentRepository.findByUserId(query.userId());
    }
}
