package nz.taskit.web.dto;

import java.time.Instant;

public record TaskChatMessageResponse(
        Long id,
        UserResponse sender,
        String message,
        Instant sentAt
) {
}
