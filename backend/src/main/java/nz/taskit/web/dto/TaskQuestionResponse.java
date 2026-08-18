package nz.taskit.web.dto;

import java.time.Instant;

public record TaskQuestionResponse(
        Long id,
        UserResponse askingDoer,
        String question,
        Instant askedAt,
        String answer,
        Instant answeredAt
) {
}
