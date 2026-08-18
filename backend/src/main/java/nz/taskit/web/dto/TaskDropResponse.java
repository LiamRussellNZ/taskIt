package nz.taskit.web.dto;

import java.time.Instant;

public record TaskDropResponse(
        Long id,
        UserResponse doer,
        Instant droppedAt,
        Integer rating,
        String review,
        Instant reviewedAt
) {
}
