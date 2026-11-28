package com.spottrack.platform.iam.interfaces.rest.resources;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "User resource representation")
public record UserResource(
        @Schema(description = "User identifier", example = "42") Long id,
        @Schema(description = "Username (email)", example = "jane.doe@spottrack.com") String username,
        @ArraySchema(arraySchema = @Schema(description = "Assigned role names"),
                schema = @Schema(example = "ROLE_CLIENT")) List<String> roles,
        @Schema(description = "Notify on critical alerts", example = "true") boolean notifyOnCritical,
        @Schema(description = "Notify on warning alerts", example = "false") boolean notifyOnWarning,
        @Schema(description = "Notification email address", example = "jane.doe@spottrack.com") String notificationEmail
) {
}
