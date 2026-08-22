package nz.taskit.service;

import java.util.List;
import nz.taskit.domain.AppUser;
import nz.taskit.domain.Task;
import nz.taskit.domain.TaskStatus;
import nz.taskit.domain.UserNotificationType;
import nz.taskit.domain.UserRole;
import nz.taskit.repository.TaskAssistanceRequestRepository;
import nz.taskit.repository.TaskQuestionRepository;
import nz.taskit.repository.TaskReviewRepository;
import nz.taskit.repository.TaskRepository;
import nz.taskit.repository.StatusUpdateRequestRepository;
import nz.taskit.web.TaskView;
import nz.taskit.web.dto.StatusUpdateResponse;
import nz.taskit.web.dto.StatusUpdateWriteRequest;
import nz.taskit.web.dto.TaskAssistanceRequestResponse;
import nz.taskit.web.dto.TaskDropReviewRequest;
import nz.taskit.web.dto.TaskDropResponse;
import nz.taskit.web.dto.TaskQuestionAnswerRequest;
import nz.taskit.web.dto.TaskQuestionResponse;
import nz.taskit.web.dto.TaskQuestionWriteRequest;
import nz.taskit.web.dto.TaskCompletionReviewResponse;
import nz.taskit.web.dto.TaskReviewWriteRequest;
import nz.taskit.web.dto.TaskResponse;
import nz.taskit.web.dto.TaskPageResponse;
import nz.taskit.web.dto.TaskWriteRequest;
import nz.taskit.web.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository tasks;
    private final StatusUpdateRequestRepository statusUpdates;
    private final TaskAssistanceRequestRepository assistanceRequests;
    private final TaskQuestionRepository questions;
    private final TaskReviewRepository reviews;
    private final UserService userService;
    private final UserNotificationService notifications;

    public TaskService(
            TaskRepository tasks,
            StatusUpdateRequestRepository statusUpdates,
            TaskAssistanceRequestRepository assistanceRequests,
            TaskQuestionRepository questions,
            TaskReviewRepository reviews,
            UserService userService,
            UserNotificationService notifications
    ) {
        this.tasks = tasks;
        this.statusUpdates = statusUpdates;
        this.assistanceRequests = assistanceRequests;
        this.questions = questions;
        this.reviews = reviews;
        this.userService = userService;
        this.notifications = notifications;
    }

    @Transactional
    public TaskResponse create(Long userId, TaskWriteRequest request) {
        AppUser asker = userService.requireUser(userId);
        requireRole(asker, UserRole.ASKER);
        Task task = new Task(
                request.title().trim(),
                request.description().trim(),
                request.category().trim(),
                request.location().trim(),
                request.remote(),
                asker
        );
        return toResponse(tasks.save(task));
    }

    public List<TaskResponse> list(TaskView view, String category, Long userId) {
        List<Task> result = switch (view) {
            case OPEN -> category == null || category.isBlank()
                    ? tasks.findOpenBoardTasks(TaskStatus.OPEN, TaskStatus.CLAIMED)
                    : tasks.findOpenBoardTasksByCategory(TaskStatus.OPEN, TaskStatus.CLAIMED, category.trim());
            case MINE_AS_ASKER -> category == null || category.isBlank()
                    ? tasks.findByAskerIdOrderByCreatedAtDesc(requireUserId(userId))
                    : tasks.findByAskerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(requireUserId(userId), category.trim());
            case MINE_AS_DOER -> category == null || category.isBlank()
                    ? tasks.findByAssignedDoerIdOrderByCreatedAtDesc(requireUserId(userId))
                    : tasks.findByAssignedDoerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(requireUserId(userId), category.trim());
        };
        return result.stream().map(this::toResponse).toList();
    }

    public TaskPageResponse listPage(TaskView view, String category, Long userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Task> result = switch (view) {
            case OPEN -> category == null || category.isBlank()
                    ? tasks.findOpenBoardTasks(TaskStatus.OPEN, TaskStatus.CLAIMED, pageable)
                    : tasks.findOpenBoardTasksByCategory(TaskStatus.OPEN, TaskStatus.CLAIMED, category.trim(), pageable);
            case MINE_AS_ASKER -> category == null || category.isBlank()
                    ? tasks.findByAskerIdOrderByCreatedAtDesc(requireUserId(userId), pageable)
                    : tasks.findByAskerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(requireUserId(userId), category.trim(), pageable);
            case MINE_AS_DOER -> category == null || category.isBlank()
                    ? tasks.findByAssignedDoerIdOrderByCreatedAtDesc(requireUserId(userId), pageable)
                    : tasks.findByAssignedDoerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(requireUserId(userId), category.trim(), pageable);
        };
        return new TaskPageResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public TaskResponse get(Long taskId) {
        return toResponse(requireTask(taskId));
    }

    @Transactional
    public TaskResponse update(Long userId, Long taskId, TaskWriteRequest request) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        requireAsker(actor, task);
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new ConflictException("Only open tasks can be edited");
        }
        task.updateDetails(
                request.title().trim(),
                request.description().trim(),
                request.category().trim(),
                request.location().trim(),
                request.remote()
        );
        return toResponse(task);
    }

    @Transactional
    public TaskResponse claim(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        requireRole(actor, UserRole.DOER);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new ConflictException("Only open tasks can be claimed");
        }
        if (task.getAsker().getId().equals(actor.getId())) {
            throw new ConflictException("You cannot claim your own task");
        }
        if (task.wasDroppedBy(actor)) {
            throw new ConflictException("You cannot claim a task you previously dropped");
        }
        task.claim(actor);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse complete(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new ConflictException("Only claimed tasks can be completed");
        }
        boolean isAsker = task.getAsker().getId().equals(actor.getId());
        boolean isAssignedDoer = task.getAssignedDoer() != null
                && task.getAssignedDoer().getId().equals(actor.getId());
        if (!isAsker && !isAssignedDoer) {
            throw new ForbiddenException("Only the task asker or assigned doer can complete this task");
        }
        task.complete();
        if (isAssignedDoer) {
            notifications.create(task.getAsker(), task, actor, UserNotificationType.TASK_COMPLETED);
        }
        return toResponse(task);
    }

    @Transactional
    public TaskResponse cancel(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        requireAsker(actor, task);
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.CANCELLED) {
            throw new ConflictException("Completed or cancelled tasks cannot be cancelled");
        }
        task.cancel();
        return toResponse(task);
    }

    @Transactional
    public TaskResponse drop(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new ConflictException("Only claimed tasks can be dropped");
        }
        if (task.getAssignedDoer() == null || !task.getAssignedDoer().getId().equals(actor.getId())) {
            throw new ForbiddenException("Only the assigned doer can drop this task");
        }
        task.drop();
        tasks.saveAndFlush(task);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse reviewDrop(Long userId, Long taskId, Long dropId, TaskDropReviewRequest request) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        requireAsker(actor, task);
        var drop = task.getDrops().stream()
                .filter(candidate -> candidate.getId().equals(dropId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Task drop " + dropId + " was not found for this task"));
        if (drop.getRating() != null) {
            throw new ConflictException("This task drop has already been reviewed");
        }
        String review = request.review() == null ? null : request.review().trim();
        drop.review(request.rating(), review == null || review.isEmpty() ? null : review);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse reviewCompletion(Long userId, Long taskId, TaskReviewWriteRequest request) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        requireAsker(actor, task);
        if (task.getStatus() != TaskStatus.COMPLETED) {
            throw new ConflictException("Only completed tasks can be reviewed");
        }
        if (task.getAssignedDoer() == null) {
            throw new ConflictException("A completed task without a doer cannot be reviewed");
        }
        if (task.getCompletionReview() != null) {
            throw new ConflictException("This completed task has already been reviewed");
        }
        String review = request.review() == null ? null : request.review().trim();
        reviews.saveAndFlush(task.reviewCompletion(request.rating(), review == null || review.isEmpty() ? null : review));
        return toResponse(task);
    }

    @Transactional
    public TaskResponse requestStatusUpdate(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        requireAsker(actor, task);
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new ConflictException("Status updates can only be requested for claimed tasks");
        }
        statusUpdates.save(task.requestStatusUpdate());
        return toResponse(task);
    }

    @Transactional
    public TaskResponse requestAssistance(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new ConflictException("Assistance can only be requested for claimed tasks");
        }
        if (task.getAssignedDoer() == null || !task.getAssignedDoer().getId().equals(actor.getId())) {
            throw new ForbiddenException("Only the assigned doer can request assistance");
        }
        if (!task.getAssistanceRequests().isEmpty()) {
            throw new ConflictException("Assistance has already been requested for this task");
        }
        assistanceRequests.saveAndFlush(task.requestAssistance(actor));
        notifications.create(task.getAsker(), task, actor, UserNotificationType.ASSISTANCE_REQUEST);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse offerAssistance(Long userId, Long taskId) {
        AppUser actor = userService.requireUser(userId);
        requireRole(actor, UserRole.DOER);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new ConflictException("Assistance can only be offered for claimed tasks");
        }
        if (task.getAsker().getId().equals(actor.getId())
                || task.getAssignedDoer() == null
                || task.getAssignedDoer().getId().equals(actor.getId())) {
            throw new ForbiddenException("The task asker and assigned doer cannot offer assistance");
        }
        var assistanceRequest = assistanceRequests.findByTaskIdForUpdate(taskId)
                .orElseThrow(() -> new NotFoundException("No assistance request was found for this task"));
        if (assistanceRequest.getHelper() != null) {
            throw new ConflictException("Assistance has already been offered for this task");
        }
        assistanceRequest.offer(actor);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse askQuestion(Long userId, Long taskId, TaskQuestionWriteRequest request) {
        AppUser actor = userService.requireUser(userId);
        requireRole(actor, UserRole.DOER);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.OPEN) {
            throw new ConflictException("Questions can only be asked on open tasks");
        }
        questions.save(task.askQuestion(actor, request.question().trim()));
        notifications.create(task.getAsker(), task, actor, UserNotificationType.TASK_QUESTION);
        return toResponse(task);
    }

    @Transactional
    public TaskResponse answerQuestion(Long userId, Long taskId, Long questionId, TaskQuestionAnswerRequest request) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        requireAsker(actor, task);
        var question = task.getQuestions().stream()
                .filter(candidate -> candidate.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Question " + questionId + " was not found for this task"));
        if (question.getAnswer() != null) {
            throw new ConflictException("This question has already been answered");
        }
        question.answer(request.answer().trim());
        return toResponse(task);
    }

    @Transactional
    public TaskResponse respondToStatusUpdate(Long userId, Long taskId, Long statusUpdateId, StatusUpdateWriteRequest request) {
        AppUser actor = userService.requireUser(userId);
        Task task = requireTask(taskId);
        if (task.getStatus() != TaskStatus.CLAIMED) {
            throw new ConflictException("Status updates can only be answered for claimed tasks");
        }
        if (task.getAssignedDoer() == null || !task.getAssignedDoer().getId().equals(actor.getId())) {
            throw new ForbiddenException("Only the assigned doer can provide a status update");
        }
        var statusUpdate = statusUpdates.findById(statusUpdateId)
                .orElseThrow(() -> new NotFoundException("Status update request " + statusUpdateId + " was not found"));
        if (!task.getStatusUpdateRequests().contains(statusUpdate)) {
            throw new NotFoundException("Status update request " + statusUpdateId + " was not found for this task");
        }
        if (statusUpdate.getResponse() != null) {
            throw new ConflictException("This status update request has already been answered");
        }
        statusUpdate.respond(request.response().trim());
        return toResponse(task);
    }

    private Long requireUserId(Long userId) {
        if (userId == null) {
            throw new ForbiddenException("X-User-Id is required for personal task views");
        }
        userService.requireUser(userId);
        return userId;
    }

    private Task requireTask(Long id) {
        return tasks.findById(id).orElseThrow(() -> new NotFoundException("Task " + id + " was not found"));
    }

    private void requireRole(AppUser user, UserRole role) {
        if (!user.getRoles().contains(role)) {
            throw new ForbiddenException("User requires the " + role + " role");
        }
    }

    private void requireAsker(AppUser actor, Task task) {
        if (!task.getAsker().getId().equals(actor.getId())) {
            throw new ForbiddenException("Only the task asker may perform this action");
        }
    }

    private TaskResponse toResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCategory(),
                task.getLocation(),
                task.isRemote(),
                task.getStatus(),
                userService.toResponse(task.getAsker()),
                task.getAssignedDoer() == null ? null : userService.toResponse(task.getAssignedDoer()),
                task.getAssistanceRequests().stream()
                        .findFirst()
                        .map(request -> new TaskAssistanceRequestResponse(
                                request.getId(),
                                userService.toResponse(request.getRequestingDoer()),
                                request.getHelper() == null ? null : userService.toResponse(request.getHelper()),
                                request.getRequestedAt(),
                                request.getOfferedAt()
                        ))
                        .orElse(null),
                task.getQuestions().stream()
                        .map(question -> new TaskQuestionResponse(
                                question.getId(),
                                userService.toResponse(question.getAskingDoer()),
                                question.getQuestion(),
                                question.getAskedAt(),
                                question.getAnswer(),
                                question.getAnsweredAt()
                        ))
                        .toList(),
                task.getStatusUpdateRequests().stream()
                        .map(statusUpdate -> new StatusUpdateResponse(
                                statusUpdate.getId(),
                                statusUpdate.getRequestedAt(),
                                statusUpdate.getResponse(),
                                statusUpdate.getRespondedAt()
                        ))
                        .toList(),
                task.getCompletionReview() == null ? null : new TaskCompletionReviewResponse(
                        task.getCompletionReview().getId(),
                        userService.toResponse(task.getCompletionReview().getDoer()),
                        task.getCompletionReview().getRating(),
                        task.getCompletionReview().getReview(),
                        task.getCompletionReview().getReviewedAt()
                ),
                task.getDrops().stream()
                        .map(drop -> new TaskDropResponse(
                                drop.getId(),
                                userService.toResponse(drop.getDoer()),
                                drop.getDroppedAt(),
                                drop.getRating(),
                                drop.getReview(),
                                drop.getReviewedAt()
                        ))
                        .toList(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
