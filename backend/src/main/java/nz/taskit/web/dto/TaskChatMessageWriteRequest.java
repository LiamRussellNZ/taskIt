package nz.taskit.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskChatMessageWriteRequest(@NotBlank @Size(max = 2000) String message) {
}
