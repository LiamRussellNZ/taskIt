package nz.taskit.web.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import nz.taskit.domain.UserRole;

public record UserRolesUpdateRequest(@NotEmpty Set<UserRole> roles) {
}
