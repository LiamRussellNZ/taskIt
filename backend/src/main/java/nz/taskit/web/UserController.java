package nz.taskit.web;

import jakarta.validation.Valid;
import java.util.List;
import nz.taskit.service.UserService;
import nz.taskit.service.UserNotificationService;
import nz.taskit.web.dto.UserCreateRequest;
import nz.taskit.web.dto.UserNotificationResponse;
import nz.taskit.web.dto.UserProfileResponse;
import nz.taskit.web.dto.UserResponse;
import nz.taskit.web.dto.UserRolesUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService users;
    private final UserNotificationService notifications;

    public UserController(UserService users, UserNotificationService notifications) {
        this.users = users;
        this.notifications = notifications;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@Valid @RequestBody UserCreateRequest request) {
        return users.create(request);
    }

    @GetMapping
    public List<UserResponse> list() {
        return users.list();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return users.get(id);
    }

    @GetMapping("/{id}/profile")
    public UserProfileResponse profile(@PathVariable Long id) {
        return users.profile(id);
    }

    @GetMapping("/{id}/notifications")
    public List<UserNotificationResponse> notifications(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return notifications.list(id, userId);
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UserRolesUpdateRequest request) {
        return users.updateRoles(id, request);
    }
}
