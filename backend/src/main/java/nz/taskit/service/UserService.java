package nz.taskit.service;

import java.util.List;
import nz.taskit.domain.AppUser;
import nz.taskit.domain.Task;
import nz.taskit.domain.TaskDrop;
import nz.taskit.domain.TaskReview;
import nz.taskit.repository.AppUserRepository;
import nz.taskit.repository.TaskDropRepository;
import nz.taskit.repository.TaskRepository;
import nz.taskit.repository.TaskReviewRepository;
import nz.taskit.web.dto.ProfileDroppedAssignmentResponse;
import nz.taskit.web.dto.ProfileReviewResponse;
import nz.taskit.web.dto.ProfileTaskResponse;
import nz.taskit.web.dto.UserCreateRequest;
import nz.taskit.web.dto.UserProfileResponse;
import nz.taskit.web.dto.UserResponse;
import nz.taskit.web.dto.UserRolesUpdateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final AppUserRepository users;
    private final TaskRepository tasks;
    private final TaskDropRepository drops;
    private final TaskReviewRepository reviews;

    public UserService(AppUserRepository users, TaskRepository tasks, TaskDropRepository drops, TaskReviewRepository reviews) {
        this.users = users;
        this.tasks = tasks;
        this.drops = drops;
        this.reviews = reviews;
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        if (users.findByEmailIgnoreCase(request.email().trim()).isPresent()) {
            throw new ConflictException("Email is already registered");
        }
        try {
            return toResponse(users.save(new AppUser(
                    request.displayName().trim(),
                    request.email().trim().toLowerCase(),
                    request.roles()
            )));
        } catch (DataIntegrityViolationException exception) {
            throw new ConflictException("Email is already registered");
        }
    }

    public List<UserResponse> list() {
        return users.findAll().stream().map(this::toResponse).toList();
    }

    public UserResponse get(Long id) {
        return toResponse(requireUser(id));
    }

    public UserProfileResponse profile(Long id) {
        AppUser user = requireUser(id);
        List<TaskDrop> droppedAssignments = drops.findByDoerIdWithTaskOrderByDroppedAtDesc(id);
        List<TaskReview> completionReviews = reviews.findByDoerIdWithTaskOrderByReviewedAtDesc(id);
        var average = completionReviews.stream()
                .mapToInt(TaskReview::getRating)
                .average();
        Double averageRating = average.isPresent() ? average.getAsDouble() : null;

        return new UserProfileResponse(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getRoles(),
                averageRating,
                completionReviews.stream()
                        .filter(review -> review.getReview() != null && !review.getReview().isBlank())
                        .map(review -> new ProfileReviewResponse(
                                toProfileTask(review.getTask()),
                                review.getRating(),
                                review.getReview(),
                                review.getReviewedAt()
                        ))
                        .toList(),
                tasks.findByAskerIdOrderByCreatedAtDesc(id).stream().map(this::toProfileTask).toList(),
                tasks.findByAssignedDoerIdOrderByCreatedAtDesc(id).stream().map(this::toProfileTask).toList(),
                droppedAssignments.stream()
                        .map(drop -> new ProfileDroppedAssignmentResponse(
                                toProfileTask(drop.getTask()),
                                drop.getDroppedAt(),
                                drop.getRating(),
                                drop.getReview(),
                                drop.getReviewedAt()
                        ))
                        .toList()
        );
    }

    @Transactional
    public UserResponse updateRoles(Long id, UserRolesUpdateRequest request) {
        AppUser user = requireUser(id);
        user.setRoles(request.roles());
        return toResponse(user);
    }

    public AppUser requireUser(Long id) {
        return users.findById(id).orElseThrow(() -> new NotFoundException("User " + id + " was not found"));
    }

    public UserResponse toResponse(AppUser user) {
        return new UserResponse(user.getId(), user.getDisplayName(), user.getEmail(), user.getRoles());
    }

    private ProfileTaskResponse toProfileTask(Task task) {
        return new ProfileTaskResponse(
                task.getId(),
                task.getTitle(),
                task.getCategory(),
                task.getLocation(),
                task.isRemote(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
