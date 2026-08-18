package nz.taskit.service;

import java.util.List;
import nz.taskit.domain.AppUser;
import nz.taskit.domain.Task;
import nz.taskit.domain.UserNotification;
import nz.taskit.domain.UserNotificationType;
import nz.taskit.repository.UserNotificationRepository;
import nz.taskit.web.dto.UserNotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserNotificationService {

    private final UserNotificationRepository notifications;
    private final UserService users;

    public UserNotificationService(UserNotificationRepository notifications, UserService users) {
        this.notifications = notifications;
        this.users = users;
    }

    @Transactional
    public void create(AppUser recipient, Task task, AppUser actor, UserNotificationType type) {
        UserNotification notification = new UserNotification(recipient, task, actor, type);
        task.addNotification(notification);
        notifications.save(notification);
    }

    public List<UserNotificationResponse> list(Long requestedUserId, Long actorId) {
        if (!requestedUserId.equals(actorId)) {
            throw new ForbiddenException("You may only view your own notifications");
        }
        users.requireUser(requestedUserId);
        return notifications.findByRecipientIdOrderByCreatedAtDesc(requestedUserId).stream()
                .map(this::toResponse)
                .toList();
    }

    private UserNotificationResponse toResponse(UserNotification notification) {
        return new UserNotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTask().getId(),
                notification.getTask().getTitle(),
                users.toResponse(notification.getActor()),
                notification.getCreatedAt()
        );
    }
}
