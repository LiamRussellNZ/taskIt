package nz.taskit.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StatusUpdateWriteRequest(
        @NotBlank @Size(max = 2000) String response
) {
}
