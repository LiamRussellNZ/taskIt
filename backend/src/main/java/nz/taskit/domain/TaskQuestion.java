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
@Table(name = "task_questions")
public class TaskQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asking_doer_id", nullable = false)
    private AppUser askingDoer;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(nullable = false, updatable = false)
    private Instant askedAt;

    @Column(length = 2000)
    private String answer;

    private Instant answeredAt;

    protected TaskQuestion() {
    }

    public TaskQuestion(Task task, AppUser askingDoer, String question) {
        this.task = task;
        this.askingDoer = askingDoer;
        this.question = question;
    }

    @PrePersist
    void setAskedTimestamp() {
        askedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getAskingDoer() {
        return askingDoer;
    }

    public String getQuestion() {
        return question;
    }

    public Instant getAskedAt() {
        return askedAt;
    }

    public String getAnswer() {
        return answer;
    }

    public Instant getAnsweredAt() {
        return answeredAt;
    }

    public void answer(String answer) {
        this.answer = answer;
        this.answeredAt = Instant.now();
    }
}
