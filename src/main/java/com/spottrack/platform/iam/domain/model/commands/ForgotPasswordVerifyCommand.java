package com.spottrack.platform.iam.domain.model.commands;

public record ForgotPasswordVerifyCommand(String email, String dni, String newPassword) {
}
