package com.spottrack.platform.membership.infrastructure.persistence.jpa.adapters;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.valueobjects.PaymentId;
import com.spottrack.platform.membership.domain.repositories.PaymentRepository;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.assemblers.PaymentPersistenceAssembler;
import com.spottrack.platform.membership.infrastructure.persistence.jpa.repositories.PaymentPersistenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepositoryImpl implements PaymentRepository {

    private final PaymentPersistenceRepository paymentPersistenceRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PaymentRepositoryImpl(
            PaymentPersistenceRepository paymentPersistenceRepository,
            ApplicationEventPublisher eventPublisher) {
        this.paymentPersistenceRepository = paymentPersistenceRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Payment> findByPaymentId(PaymentId paymentId) {
        return paymentPersistenceRepository.findByPaymentId(paymentId.uuid().toString())
                .map(PaymentPersistenceAssembler::toDomainFromPersistence);
    }

    @Override
    public List<Payment> findByUserId(Long userId) {
        return paymentPersistenceRepository.findByUserId(userId).stream()
                .map(PaymentPersistenceAssembler::toDomainFromPersistence)
                .toList();
    }

    @Override
    public Payment save(Payment payment) {
        boolean isNew = payment.getId() == null;
        var savedEntity = paymentPersistenceRepository.save(
                PaymentPersistenceAssembler.toPersistenceFromDomain(payment));
        var savedPayment = PaymentPersistenceAssembler.toDomainFromPersistence(savedEntity);
        if (isNew) {
            savedPayment.onCreated();
            savedPayment.domainEvents().forEach(eventPublisher::publishEvent);
            savedPayment.clearDomainEvents();
        } else {
            payment.domainEvents().forEach(eventPublisher::publishEvent);
            payment.clearDomainEvents();
        }
        return savedPayment;
    }
}
