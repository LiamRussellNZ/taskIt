package nz.taskit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 160)
    private String location;

    @Column(nullable = false)
    private boolean remote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TaskStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "asker_id", nullable = false)
    private AppUser asker;

    @ManyToOne
    @JoinColumn(name = "assigned_doer_id")
    private AppUser assignedDoer;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("requestedAt DESC")
    private List<StatusUpdateRequest> statusUpdateRequests = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("droppedAt DESC")
    private List<TaskDrop> drops = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskAssistanceRequest> assistanceRequests = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("askedAt ASC")
    private List<TaskQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserNotification> notifications = new ArrayList<>();

    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private TaskReview completionReview;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected Task() {
    }

    public Task(String title, String description, String category, String location, boolean remote, AppUser asker) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.remote = remote;
        this.asker = asker;
        this.status = TaskStatus.OPEN;
    }

    @PrePersist
    void setCreationTimestamp() {
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void setUpdateTimestamp() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    public boolean isRemote() {
        return remote;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public AppUser getAsker() {
        return asker;
    }

    public AppUser getAssignedDoer() {
        return assignedDoer;
    }

    public List<StatusUpdateRequest> getStatusUpdateRequests() {
        return statusUpdateRequests;
    }

    public List<TaskDrop> getDrops() {
        return drops;
    }

    public List<TaskAssistanceRequest> getAssistanceRequests() {
        return assistanceRequests;
    }

    public List<TaskQuestion> getQuestions() {
        return questions;
    }

    public TaskReview getCompletionReview() {
        return completionReview;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void updateDetails(String title, String description, String category, String location, boolean remote) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.remote = remote;
        touch();
    }

    public void claim(AppUser doer) {
        this.assignedDoer = doer;
        this.status = TaskStatus.CLAIMED;
        touch();
    }

    public void complete() {
        this.status = TaskStatus.COMPLETED;
        touch();
    }

    public void cancel() {
        this.status = TaskStatus.CANCELLED;
        touch();
    }

    public TaskDrop drop() {
        TaskDrop drop = new TaskDrop(this, assignedDoer);
        drops.add(drop);
        assistanceRequests.clear();
        assignedDoer = null;
        status = TaskStatus.OPEN;
        touch();
        return drop;
    }

    public boolean wasDroppedBy(AppUser doer) {
        return drops.stream().anyMatch(drop -> drop.getDoer().getId().equals(doer.getId()));
    }

    public StatusUpdateRequest requestStatusUpdate() {
        StatusUpdateRequest request = new StatusUpdateRequest(this);
        statusUpdateRequests.add(request);
        return request;
    }

    public TaskAssistanceRequest requestAssistance(AppUser requestingDoer) {
        TaskAssistanceRequest request = new TaskAssistanceRequest(this, requestingDoer);
        assistanceRequests.add(request);
        return request;
    }

    public TaskQuestion askQuestion(AppUser askingDoer, String question) {
        TaskQuestion taskQuestion = new TaskQuestion(this, askingDoer, question);
        questions.add(taskQuestion);
        return taskQuestion;
    }

    public void addNotification(UserNotification notification) {
        notifications.add(notification);
    }

    public TaskReview reviewCompletion(int rating, String review) {
        completionReview = new TaskReview(this, assignedDoer, rating, review);
        return completionReview;
    }

    private void touch() {
        updatedAt = Instant.now();
    }
}
