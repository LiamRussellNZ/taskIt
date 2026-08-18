package nz.taskit.web.dto;

import java.time.Instant;
import nz.taskit.domain.UserNotificationType;

public record UserNotificationResponse(
        Long id,
        UserNotificationType type,
        Long taskId,
        String taskTitle,
        UserResponse actor,
        Instant createdAt
) {
}
