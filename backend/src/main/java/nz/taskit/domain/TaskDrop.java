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
@Table(name = "task_drops")
public class TaskDrop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doer_id", nullable = false)
    private AppUser doer;

    @Column(nullable = false, updatable = false)
    private Instant droppedAt;

    private Integer rating;

    @Column(length = 2000)
    private String review;

    private Instant reviewedAt;

    protected TaskDrop() {
    }

    public TaskDrop(Task task, AppUser doer) {
        this.task = task;
        this.doer = doer;
    }

    @PrePersist
    void setDroppedTimestamp() {
        droppedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getDoer() {
        return doer;
    }

    public Task getTask() {
        return task;
    }

    public Instant getDroppedAt() {
        return droppedAt;
    }

    public Integer getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void review(int rating, String review) {
        this.rating = rating;
        this.review = review;
        this.reviewedAt = Instant.now();
    }
}
