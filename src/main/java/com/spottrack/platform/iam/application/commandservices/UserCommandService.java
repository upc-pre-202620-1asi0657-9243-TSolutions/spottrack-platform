package com.spottrack.platform.iam.application.commandservices;

import com.spottrack.platform.iam.domain.model.aggregates.User;
import com.spottrack.platform.iam.domain.model.commands.ChangePasswordCommand;
import com.spottrack.platform.iam.domain.model.commands.DeactivateAccountCommand;
import com.spottrack.platform.iam.domain.model.commands.ForgotPasswordVerifyCommand;
import com.spottrack.platform.iam.domain.model.commands.ProvisionIamAccountCommand;
import com.spottrack.platform.iam.domain.model.commands.SignInCommand;
import com.spottrack.platform.iam.domain.model.commands.SignOutCommand;
import com.spottrack.platform.iam.domain.model.commands.SignUpCommand;
import com.spottrack.platform.iam.domain.model.commands.UpdateNotificationPreferencesCommand;
import com.spottrack.platform.shared.application.result.ApplicationError;
import com.spottrack.platform.shared.application.result.Result;
import org.apache.commons.lang3.tuple.ImmutablePair;

public interface UserCommandService {
    Result<User, ApplicationError> handle(SignUpCommand command);
    Result<ImmutablePair<User, String>, ApplicationError> handle(SignInCommand command);
    Result<User, ApplicationError> handle(ChangePasswordCommand command);
    Result<User, ApplicationError> handle(ForgotPasswordVerifyCommand command);
    Result<User, ApplicationError> handle(SignOutCommand command);
    Result<User, ApplicationError> handle(DeactivateAccountCommand command);
    Result<User, ApplicationError> handle(ProvisionIamAccountCommand command);
    Result<User, ApplicationError> handle(UpdateNotificationPreferencesCommand command);
}
