package nz.taskit.web.dto;

import java.time.Instant;

public record ProfileReviewResponse(
        ProfileTaskResponse task,
        int rating,
        String review,
        Instant reviewedAt
) {
}
