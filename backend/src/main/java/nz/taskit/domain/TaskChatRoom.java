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
@Table(name = "task_chat_rooms")
public class TaskChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "primary_doer_id", nullable = false)
    private AppUser primaryDoer;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant closedAt;

    protected TaskChatRoom() {
    }

    public TaskChatRoom(Task task, AppUser primaryDoer) {
        this.task = task;
        this.primaryDoer = primaryDoer;
    }

    @PrePersist
    void setCreatedTimestamp() {
        createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public AppUser getPrimaryDoer() {
        return primaryDoer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void close() {
        closedAt = Instant.now();
    }
}
