package nz.taskit.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import nz.taskit.service.TaskService;
import nz.taskit.web.dto.TaskPageResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v2/tasks")
public class TaskV2Controller {

    private final TaskService tasks;

    public TaskV2Controller(TaskService tasks) {
        this.tasks = tasks;
    }

    @GetMapping
    public TaskPageResponse list(
            @RequestParam(defaultValue = "OPEN") TaskView view,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "25") @Min(1) @Max(75) int size,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        return tasks.listPage(view, category, userId, page, size);
    }
}
