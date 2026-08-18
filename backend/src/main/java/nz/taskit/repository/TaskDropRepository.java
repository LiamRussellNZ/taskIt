package nz.taskit.repository;

import java.util.List;
import nz.taskit.domain.TaskDrop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskDropRepository extends JpaRepository<TaskDrop, Long> {

    @Query("select drop from TaskDrop drop join fetch drop.task where drop.doer.id = :doerId order by drop.droppedAt desc")
    List<TaskDrop> findByDoerIdWithTaskOrderByDroppedAtDesc(@Param("doerId") Long doerId);
}
