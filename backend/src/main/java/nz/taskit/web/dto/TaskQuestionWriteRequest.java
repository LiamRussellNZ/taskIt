package nz.taskit.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskQuestionWriteRequest(@NotBlank @Size(max = 2000) String question) {
}
