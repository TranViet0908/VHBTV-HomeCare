package Project.HouseService.Entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK BIGINT tới chat_conversation.id — cột tên "conversation_id"
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", referencedColumnName = "id", nullable = false)
    private ChatConversation conversation;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Lob
    @Column(name = "question", columnDefinition = "TEXT", nullable = false)
    private String question;

    @Lob
    @Column(name = "answer", columnDefinition = "MEDIUMTEXT")
    private String answer;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public ChatMessage() {
    }

    public ChatMessage(Long id, ChatConversation conversation, Long userId, String question, String answer, Instant createdAt) {
        this.id = id;
        this.conversation = conversation;
        this.userId = userId;
        this.question = question;
        this.answer = answer;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChatConversation getConversation() {
        return conversation;
    }

    public void setConversation(ChatConversation conversation) {
        this.conversation = conversation;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}