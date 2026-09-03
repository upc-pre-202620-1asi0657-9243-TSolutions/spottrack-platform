package com.spottrack.platform.membership.application.internal.commandservices;

import com.spottrack.platform.membership.application.commandservices.PaymentCommandService;
import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.commands.ConfirmPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.FailPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateBusinessPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateDebtPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateResubscriptionPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateUpgradePaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.PayMembershipCommand;
import com.spottrack.platform.membership.domain.repositories.PaymentRepository;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PaymentCommandServiceImpl implements PaymentCommandService {

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    private final PaymentRepository paymentRepository;

    public PaymentCommandServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Result<String, ApplicationError> handle(PayMembershipCommand command) {
        try {
            var payment = new Payment(command);
            var saved = paymentRepository.save(payment);

            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(command.amount().currency().toLowerCase())
                                    .setUnitAmount(command.amount().amount().longValueExact() * 100L)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("SpotTrack Membership: " + command.membershipTier().name())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("paymentId", saved.getPaymentId().uuid().toString())
                    .build();

            Session session = Session.create(params);
            return Result.success(session.getUrl());

        } catch (StripeException e) {
            return Result.failure(ApplicationError.unexpected("Stripe session creation", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("Payment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Payment recording", e.getMessage()));
        }
    }

    @Override
    public Result<String, ApplicationError> handle(InitiateBusinessPaymentCommand command) {
        try {
            var payment = new Payment(command);
            var saved = paymentRepository.save(payment);

            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(command.amount().currency().toLowerCase())
                                    .setUnitAmount(command.amount().amount().longValueExact() * 100L)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("SpotTrack Membership: " + command.membershipTier().name())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("paymentId", saved.getPaymentId().uuid().toString())
                    .putMetadata("pendingRegistrationId", command.pendingRegistrationId().toString())
                    .build();

            Session session = Session.create(params);
            return Result.success(session.getUrl());

        } catch (StripeException e) {
            return Result.failure(ApplicationError.unexpected("Stripe session creation", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("BusinessPayment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Business payment recording", e.getMessage()));
        }
    }

    @Override
    public Result<String, ApplicationError> handle(InitiateDebtPaymentCommand command) {
        try {
            var payment = new Payment(command);
            var saved = paymentRepository.save(payment);

            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(command.amount().currency().toLowerCase())
                                    .setUnitAmount(command.amount().amount().longValueExact() * 100L)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("SpotTrack Debt Renewal: " + command.membershipTier().name())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("paymentId", saved.getPaymentId().uuid().toString())
                    .build();

            Session session = Session.create(params);
            return Result.success(session.getUrl());

        } catch (StripeException e) {
            return Result.failure(ApplicationError.unexpected("Stripe session creation", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("DebtPayment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Debt payment recording", e.getMessage()));
        }
    }

    @Override
    public Result<String, ApplicationError> handle(InitiateUpgradePaymentCommand command) {
        try {
            var payment = new Payment(command);
            var saved = paymentRepository.save(payment);

            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(command.amount().currency().toLowerCase())
                                    .setUnitAmount(command.amount().amount().longValueExact() * 100L)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("SpotTrack Plan Upgrade: " + command.newMembershipTier().name())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("paymentId", saved.getPaymentId().uuid().toString())
                    .build();

            Session session = Session.create(params);
            return Result.success(session.getUrl());

        } catch (StripeException e) {
            return Result.failure(ApplicationError.unexpected("Stripe session creation", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("UpgradePayment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Upgrade payment recording", e.getMessage()));
        }
    }

    @Override
    public Result<String, ApplicationError> handle(InitiateResubscriptionPaymentCommand command) {
        try {
            var payment = new Payment(command);
            var saved = paymentRepository.save(payment);

            var params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setQuantity(1L)
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency(command.amount().currency().toLowerCase())
                                    .setUnitAmount(command.amount().amount().longValueExact() * 100L)
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("SpotTrack Resubscription: " + command.membershipTier().name())
                                            .build())
                                    .build())
                            .build())
                    .putMetadata("paymentId", saved.getPaymentId().uuid().toString())
                    .build();

            Session session = Session.create(params);
            return Result.success(session.getUrl());

        } catch (StripeException e) {
            return Result.failure(ApplicationError.unexpected("Stripe session creation", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return Result.failure(ApplicationError.validationError("ResubscriptionPayment", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Resubscription payment recording", e.getMessage()));
        }
    }

    @Override
    public Result<Payment, ApplicationError> handle(ConfirmPaymentCommand command) {
        try {
            var payment = paymentRepository.findByPaymentId(command.paymentId());
            if (payment.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Payment", command.paymentId().uuid().toString()));
            }
            payment.get().confirm(command.gatewayTransactionId());
            var saved = paymentRepository.save(payment.get());
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation("Payment.confirm", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Payment confirmation", e.getMessage()));
        }
    }

    @Override
    public Result<Payment, ApplicationError> handle(FailPaymentCommand command) {
        try {
            var payment = paymentRepository.findByPaymentId(command.paymentId());
            if (payment.isEmpty()) {
                return Result.failure(ApplicationError.notFound("Payment", command.paymentId().uuid().toString()));
            }
            payment.get().fail();
            var saved = paymentRepository.save(payment.get());
            return Result.success(saved);
        } catch (IllegalStateException e) {
            return Result.failure(ApplicationError.businessRuleViolation("Payment.fail", e.getMessage()));
        } catch (Exception e) {
            return Result.failure(ApplicationError.unexpected("Payment failure recording", e.getMessage()));
        }
    }
}
