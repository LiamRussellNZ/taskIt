package nz.taskit.web.dto;

import java.time.Instant;
import java.util.List;
import nz.taskit.domain.TaskStatus;

public record TaskResponse(
        Long id,
        String title,
        String description,
        String category,
        String location,
        boolean remote,
        TaskStatus status,
        UserResponse asker,
        UserResponse assignedDoer,
        TaskAssistanceRequestResponse assistanceRequest,
        List<TaskQuestionResponse> questions,
        List<StatusUpdateResponse> statusUpdates,
        TaskCompletionReviewResponse completionReview,
        List<TaskDropResponse> drops,
        Instant createdAt,
        Instant updatedAt
) {
}
