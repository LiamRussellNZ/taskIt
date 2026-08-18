package nz.taskit.web.dto;

import java.time.Instant;
import nz.taskit.domain.TaskStatus;

public record ProfileTaskResponse(
        Long id,
        String title,
        String category,
        String location,
        boolean remote,
        TaskStatus status,
        Instant createdAt,
        Instant updatedAt
) {
}
