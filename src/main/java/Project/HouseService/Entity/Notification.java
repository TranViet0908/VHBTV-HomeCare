// src/main/java/Project/HouseService/Entity/Notification.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification",
        indexes = {
                @Index(name = "idx_notification_type_created", columnList = "type, created_at"),
                @Index(name = "idx_notification_rel", columnList = "related_type, related_id")
        })
public class Notification {

    public enum Type { ORDER, PAYMENT, WISHLIST, PROMOTION }
    public enum RelatedType { SERVICE_ORDER, PAYMENT, VENDOR_SERVICE, COUPON }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false,
            columnDefinition = "ENUM('ORDER','PAYMENT','WISHLIST','PROMOTION')")
    private Type type;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_type",
            columnDefinition = "ENUM('SERVICE_ORDER','PAYMENT','VENDOR_SERVICE','COUPON')")
    private RelatedType relatedType;

    @Column(name = "related_id")
    private Long relatedId;

    // Để tương thích rộng, lưu JSON dưới dạng TEXT; nếu bạn dùng MySQL JSON có thể đổi columnDefinition="JSON"
    @Column(name = "data_json", columnDefinition = "TEXT")
    private String dataJson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id",
            foreignKey = @ForeignKey(name = "fk_notification_actor_user"))
    private User actorUser;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // getters/setters
    public Long getId() { return id; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public RelatedType getRelatedType() { return relatedType; }
    public void setRelatedType(RelatedType relatedType) { this.relatedType = relatedType; }
    public Long getRelatedId() { return relatedId; }
    public void setRelatedId(Long relatedId) { this.relatedId = relatedId; }
    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }
    public User getActorUser() { return actorUser; }
    public void setActorUser(User actorUser) { this.actorUser = actorUser; }
    public Instant getCreatedAt() { return createdAt; }
}
