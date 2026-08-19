package nz.taskit.repository;

import java.util.List;
import nz.taskit.domain.TaskChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskChatMessageRepository extends JpaRepository<TaskChatMessage, Long> {
    List<TaskChatMessage> findByRoomIdOrderBySentAtAscIdAsc(Long roomId);
}
