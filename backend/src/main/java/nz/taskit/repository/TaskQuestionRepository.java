package nz.taskit.repository;

import nz.taskit.domain.TaskQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskQuestionRepository extends JpaRepository<TaskQuestion, Long> {
}
