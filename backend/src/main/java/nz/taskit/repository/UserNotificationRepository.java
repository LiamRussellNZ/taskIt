package nz.taskit.repository;

import java.util.List;
import nz.taskit.domain.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);
}
