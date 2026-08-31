package nz.taskit.repository;

import java.util.List;
import nz.taskit.domain.Task;
import nz.taskit.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    List<Task> findByStatusAndCategoryIgnoreCaseOrderByCreatedAtDesc(TaskStatus status, String category);

    @Query("""
            select distinct task from Task task
            left join task.assistanceRequests assistance
            where task.status = :openStatus
               or (task.status = :claimedStatus and assistance.id is not null and assistance.helper is null)
            order by task.createdAt desc
            """)
    List<Task> findOpenBoardTasks(
            @Param("openStatus") TaskStatus openStatus,
            @Param("claimedStatus") TaskStatus claimedStatus
    );

    @Query(
            value = """
                    select distinct task from Task task
                    left join task.assistanceRequests assistance
                    where task.status = :openStatus
                       or (task.status = :claimedStatus and assistance.id is not null and assistance.helper is null)
                    order by task.createdAt desc
                    """,
            countQuery = """
                    select count(distinct task) from Task task
                    left join task.assistanceRequests assistance
                    where task.status = :openStatus
                       or (task.status = :claimedStatus and assistance.id is not null and assistance.helper is null)
                    """
    )
    Page<Task> findOpenBoardTasks(
            @Param("openStatus") TaskStatus openStatus,
            @Param("claimedStatus") TaskStatus claimedStatus,
            Pageable pageable
    );

    @Query("""
            select distinct task from Task task
            left join task.assistanceRequests assistance
            where (task.status = :openStatus
               or (task.status = :claimedStatus and assistance.id is not null and assistance.helper is null))
              and lower(task.category) = lower(:category)
            order by task.createdAt desc
            """)
    List<Task> findOpenBoardTasksByCategory(
            @Param("openStatus") TaskStatus openStatus,
            @Param("claimedStatus") TaskStatus claimedStatus,
            @Param("category") String category
    );

    @Query(
            value = """
                    select distinct task from Task task
                    left join task.assistanceRequests assistance
                    where (task.status = :openStatus
                       or (task.status = :claimedStatus and assistance.id is not null and assistance.helper is null))
                      and lower(task.category) = lower(:category)
                    order by task.createdAt desc
                    """,
            countQuery = """
                    select count(distinct task) from Task task
                    left join task.assistanceRequests assistance
                    where (task.status = :openStatus
                       or (task.status = :claimedStatus and assistance.id is not null and assistance.helper is null))
                      and lower(task.category) = lower(:category)
                    """
    )
    Page<Task> findOpenBoardTasksByCategory(
            @Param("openStatus") TaskStatus openStatus,
            @Param("claimedStatus") TaskStatus claimedStatus,
            @Param("category") String category,
            Pageable pageable
    );

    List<Task> findByAskerIdOrderByCreatedAtDesc(Long askerId);

    Page<Task> findByAskerIdOrderByCreatedAtDesc(Long askerId, Pageable pageable);

    List<Task> findByAskerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(Long askerId, String category);

    Page<Task> findByAskerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(Long askerId, String category, Pageable pageable);

    List<Task> findByAssignedDoerIdOrderByCreatedAtDesc(Long doerId);

    Page<Task> findByAssignedDoerIdOrderByCreatedAtDesc(Long doerId, Pageable pageable);

    List<Task> findByAssignedDoerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(Long doerId, String category);

    Page<Task> findByAssignedDoerIdAndCategoryIgnoreCaseOrderByCreatedAtDesc(Long doerId, String category, Pageable pageable);
}
