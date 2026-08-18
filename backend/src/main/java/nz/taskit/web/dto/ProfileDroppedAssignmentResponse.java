package nz.taskit.web.dto;

import java.time.Instant;

public record ProfileDroppedAssignmentResponse(
        ProfileTaskResponse task,
        Instant droppedAt,
        Integer rating,
        String review,
        Instant reviewedAt
) {
}
