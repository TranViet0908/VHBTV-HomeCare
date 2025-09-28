package Project.HouseService.Entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "chat_conversation")
public class ChatConversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // cột VARCHAR(255) trong bảng, KHÔNG còn dùng với Ollama nhưng vẫn map để không lỗi
    @Column(name = "conversation_id")        // external id (chuỗi) – có thể null
    private String externalConversationId;

    @Column(name = "conversation_url")       // url hội thoại web – có thể null
    private String conversationUrl;

    @Column(name = "title")
    private String title;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    public ChatConversation() {
    }

    public ChatConversation(Long id, Long userId, String externalConversationId, String conversationUrl, String title, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.userId = userId;
        this.externalConversationId = externalConversationId;
        this.conversationUrl = conversationUrl;
        this.title = title;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getExternalConversationId() {
        return externalConversationId;
    }

    public void setExternalConversationId(String externalConversationId) {
        this.externalConversationId = externalConversationId;
    }

    public String getConversationUrl() {
        return conversationUrl;
    }

    public void setConversationUrl(String conversationUrl) {
        this.conversationUrl = conversationUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}