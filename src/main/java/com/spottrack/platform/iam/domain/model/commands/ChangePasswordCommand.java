package com.spottrack.platform.iam.domain.model.commands;

public record ChangePasswordCommand(String username, String currentPassword, String newPassword) {
}
