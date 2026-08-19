package nz.taskit.repository;

import java.util.Optional;
import nz.taskit.domain.TaskChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskChatRoomRepository extends JpaRepository<TaskChatRoom, Long> {
    Optional<TaskChatRoom> findByTaskIdAndClosedAtIsNull(Long taskId);
}
