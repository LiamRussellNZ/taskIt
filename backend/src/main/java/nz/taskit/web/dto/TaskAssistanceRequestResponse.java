package nz.taskit.web.dto;

import java.time.Instant;

public record TaskAssistanceRequestResponse(
        Long id,
        UserResponse requestingDoer,
        UserResponse helper,
        Instant requestedAt,
        Instant offeredAt
) {
}
