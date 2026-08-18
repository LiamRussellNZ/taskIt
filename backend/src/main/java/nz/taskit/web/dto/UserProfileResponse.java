package nz.taskit.web.dto;

import java.util.List;
import java.util.Set;
import nz.taskit.domain.UserRole;

public record UserProfileResponse(
        Long id,
        String displayName,
        String email,
        Set<UserRole> roles,
        Double averageReceivedRating,
        List<ProfileReviewResponse> reviews,
        List<ProfileTaskResponse> requestedTasks,
        List<ProfileTaskResponse> currentAssignments,
        List<ProfileDroppedAssignmentResponse> priorAssignments
) {
}
