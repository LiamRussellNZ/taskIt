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
@Table(name = "task_chat_messages")
public class TaskChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private TaskChatRoom room;

    @ManyToOne(optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private AppUser sender;

    @Column(nullable = false, length = 2000)
    private String message;

    @Column(nullable = false, updatable = false)
    private Instant sentAt;

    protected TaskChatMessage() {
    }

    public TaskChatMessage(TaskChatRoom room, AppUser sender, String message) {
        this.room = room;
        this.sender = sender;
        this.message = message;
    }

    @PrePersist
    void setSentTimestamp() {
        sentAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public TaskChatRoom getRoom() {
        return room;
    }

    public AppUser getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Instant getSentAt() {
        return sentAt;
    }
}
