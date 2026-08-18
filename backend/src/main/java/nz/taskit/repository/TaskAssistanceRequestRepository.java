package nz.taskit.repository;

import java.util.Optional;
import nz.taskit.domain.TaskAssistanceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface TaskAssistanceRequestRepository extends JpaRepository<TaskAssistanceRequest, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select request from TaskAssistanceRequest request where request.task.id = :taskId")
    Optional<TaskAssistanceRequest> findByTaskIdForUpdate(@Param("taskId") Long taskId);
}
