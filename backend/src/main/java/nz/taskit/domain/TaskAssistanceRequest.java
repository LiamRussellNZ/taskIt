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
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "task_assistance_requests", uniqueConstraints = @UniqueConstraint(columnNames = "task_id"))
public class TaskAssistanceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "requesting_doer_id", nullable = false)
    private AppUser requestingDoer;

    @ManyToOne
    @JoinColumn(name = "helper_id")
    private AppUser helper;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    private Instant offeredAt;

    protected TaskAssistanceRequest() {
    }

    public TaskAssistanceRequest(Task task, AppUser requestingDoer) {
        this.task = task;
        this.requestingDoer = requestingDoer;
    }

    @PrePersist
    void setRequestedTimestamp() {
        requestedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getRequestingDoer() {
        return requestingDoer;
    }

    public AppUser getHelper() {
        return helper;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getOfferedAt() {
        return offeredAt;
    }

    public void offer(AppUser helper) {
        this.helper = helper;
        this.offeredAt = Instant.now();
    }
}
