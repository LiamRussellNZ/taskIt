package nz.taskit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "status_update_requests")
public class StatusUpdateRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(length = 2000)
    private String response;

    private Instant respondedAt;

    protected StatusUpdateRequest() {
    }

    public StatusUpdateRequest(Task task) {
        this.task = task;
    }

    @PrePersist
    void setRequestedTimestamp() {
        requestedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public String getResponse() {
        return response;
    }

    public Instant getRespondedAt() {
        return respondedAt;
    }

    public void respond(String response) {
        this.response = response;
        this.respondedAt = Instant.now();
    }
}
