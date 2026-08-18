package nz.taskit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "task_reviews")
public class TaskReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false, unique = true)
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doer_id", nullable = false)
    private AppUser doer;

    @Column(nullable = false)
    private int rating;

    @Column(length = 2000)
    private String review;

    @Column(nullable = false, updatable = false)
    private Instant reviewedAt;

    protected TaskReview() {
    }

    public TaskReview(Task task, AppUser doer, int rating, String review) {
        this.task = task;
        this.doer = doer;
        this.rating = rating;
        this.review = review;
    }

    @PrePersist
    void setReviewedTimestamp() {
        reviewedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public Task getTask() {
        return task;
    }

    public AppUser getDoer() {
        return doer;
    }

    public int getRating() {
        return rating;
    }

    public String getReview() {
        return review;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }
}
