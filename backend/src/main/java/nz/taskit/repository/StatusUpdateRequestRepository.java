package nz.taskit.repository;

import nz.taskit.domain.StatusUpdateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusUpdateRequestRepository extends JpaRepository<StatusUpdateRequest, Long> {
}
