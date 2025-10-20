// src/main/java/Project/HouseService/Entity/NotificationUser.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notification_user",
        uniqueConstraints = @UniqueConstraint(name = "uk_notification_user", columnNames = {"notification_id","user_id"}),
        indexes = {
                @Index(name = "idx_notification_user_read", columnList = "user_id, is_read, notification_id")
        })
public class NotificationUser {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_user_notification"))
    private Notification notification;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notification_user_user"))
    private User user;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    // getters/setters
    public Long getId() { return id; }
    public Notification getNotification() { return notification; }
    public void setNotification(Notification notification) { this.notification = notification; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public Instant getReadAt() { return readAt; }
    public void setReadAt(Instant readAt) { this.readAt = readAt; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}
