package com.spottrack.platform.membership.application.commandservices;

import com.spottrack.platform.membership.domain.model.aggregates.Payment;
import com.spottrack.platform.membership.domain.model.commands.ConfirmPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.FailPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateBusinessPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateDebtPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateResubscriptionPaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.InitiateUpgradePaymentCommand;
import com.spottrack.platform.membership.domain.model.commands.PayMembershipCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;

public interface PaymentCommandService {
    Result<String, ApplicationError> handle(PayMembershipCommand command);
    Result<String, ApplicationError> handle(InitiateBusinessPaymentCommand command);
    Result<String, ApplicationError> handle(InitiateDebtPaymentCommand command);
    Result<String, ApplicationError> handle(InitiateUpgradePaymentCommand command);
    Result<String, ApplicationError> handle(InitiateResubscriptionPaymentCommand command);
    Result<Payment, ApplicationError> handle(ConfirmPaymentCommand command);
    Result<Payment, ApplicationError> handle(FailPaymentCommand command);
}
