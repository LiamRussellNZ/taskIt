package nz.taskit.web;

import jakarta.validation.Valid;
import java.util.List;
import nz.taskit.service.TaskService;
import nz.taskit.web.dto.TaskResponse;
import nz.taskit.web.dto.TaskWriteRequest;
import nz.taskit.web.dto.StatusUpdateWriteRequest;
import nz.taskit.web.dto.TaskDropReviewRequest;
import nz.taskit.web.dto.TaskQuestionAnswerRequest;
import nz.taskit.web.dto.TaskQuestionWriteRequest;
import nz.taskit.web.dto.TaskReviewWriteRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService tasks;

    public TaskController(TaskService tasks) {
        this.tasks = tasks;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody TaskWriteRequest request
    ) {
        return tasks.create(userId, request);
    }

    @GetMapping
    public List<TaskResponse> list(
            @RequestParam(defaultValue = "OPEN") TaskView view,
            @RequestParam(required = false) String category,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        return tasks.list(view, category, userId);
    }

    @GetMapping("/{id}")
    public TaskResponse get(@PathVariable Long id) {
        return tasks.get(id);
    }

    @PatchMapping("/{id}")
    public TaskResponse update(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TaskWriteRequest request
    ) {
        return tasks.update(userId, id, request);
    }

    @PostMapping("/{id}/claim")
    public TaskResponse claim(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.claim(userId, id);
    }

    @PostMapping("/{id}/complete")
    public TaskResponse complete(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.complete(userId, id);
    }

    @PostMapping("/{id}/cancel")
    public TaskResponse cancel(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.cancel(userId, id);
    }

    @PostMapping("/{id}/drop")
    public TaskResponse drop(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.drop(userId, id);
    }

    @PostMapping("/{id}/drops/{dropId}/review")
    public TaskResponse reviewDrop(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long dropId,
            @Valid @RequestBody TaskDropReviewRequest request
    ) {
        return tasks.reviewDrop(userId, id, dropId, request);
    }

    @PostMapping("/{id}/completion-review")
    public TaskResponse reviewCompletion(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TaskReviewWriteRequest request
    ) {
        return tasks.reviewCompletion(userId, id, request);
    }

    @PostMapping("/{id}/status-updates")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse requestStatusUpdate(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.requestStatusUpdate(userId, id);
    }

    @PostMapping("/{id}/status-updates/{statusUpdateId}/respond")
    public TaskResponse respondToStatusUpdate(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long statusUpdateId,
            @Valid @RequestBody StatusUpdateWriteRequest request
    ) {
        return tasks.respondToStatusUpdate(userId, id, statusUpdateId, request);
    }

    @PostMapping("/{id}/assistance-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse requestAssistance(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.requestAssistance(userId, id);
    }

    @PostMapping("/{id}/assistance-requests/offer")
    public TaskResponse offerAssistance(@RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        return tasks.offerAssistance(userId, id);
    }

    @PostMapping("/{id}/questions")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse askQuestion(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody TaskQuestionWriteRequest request
    ) {
        return tasks.askQuestion(userId, id, request);
    }

    @PostMapping("/{id}/questions/{questionId}/answer")
    public TaskResponse answerQuestion(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @PathVariable Long questionId,
            @Valid @RequestBody TaskQuestionAnswerRequest request
    ) {
        return tasks.answerQuestion(userId, id, questionId, request);
    }
}
