package nz.taskit.web.dto;

import java.time.Instant;

public record StatusUpdateResponse(
        Long id,
        Instant requestedAt,
        String response,
        Instant respondedAt
) {
}
