package nz.taskit.web.dto;

import java.time.Instant;

public record TaskCompletionReviewResponse(
        Long id,
        UserResponse doer,
        int rating,
        String review,
        Instant reviewedAt
) {
}
