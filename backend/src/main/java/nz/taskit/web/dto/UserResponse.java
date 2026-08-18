package nz.taskit.web.dto;

import java.util.Set;
import nz.taskit.domain.UserRole;

public record UserResponse(Long id, String displayName, String email, Set<UserRole> roles) {
}
