package nz.taskit.repository;

import java.util.List;
import nz.taskit.domain.TaskReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskReviewRepository extends JpaRepository<TaskReview, Long> {

    @Query("select review from TaskReview review join fetch review.task where review.doer.id = :doerId order by review.reviewedAt desc")
    List<TaskReview> findByDoerIdWithTaskOrderByReviewedAtDesc(@Param("doerId") Long doerId);
}
