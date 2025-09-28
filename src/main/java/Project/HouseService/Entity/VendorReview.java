// src/main/java/Project/HouseService/Entity/VendorReview.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_review",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_vendor_review_so", columnNames = "service_order_id")
        })
public class VendorReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    // Khớp CSDL: mỗi đơn chỉ 1 review
    @Column(name = "service_order_id", nullable = false)
    private Long serviceOrderId;

    @Column(name = "rating", nullable = false, columnDefinition = "TINYINT UNSIGNED")
    private Integer rating; // 1..5

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Ẩn/hiện
    @Column(name = "hidden", nullable = false)
    private boolean hidden = false;

    @Column(name = "hidden_reason", length = 255)
    private String hiddenReason;

    @Column(name = "hidden_by_admin_id")
    private Long hiddenByAdminId;

    @Column(name = "hidden_at")
    private LocalDateTime hiddenAt;

    // Enum đặt tại chỗ theo yêu cầu
    public enum Visibility { VISIBLE, HIDDEN }

    public VendorReview(){}

    public VendorReview(Long id, Long vendorId, Long customerId, Long serviceOrderId, Integer rating, String content, LocalDateTime createdAt, boolean hidden, String hiddenReason, Long hiddenByAdminId, LocalDateTime hiddenAt) {
        this.id = id;
        this.vendorId = vendorId;
        this.customerId = customerId;
        this.serviceOrderId = serviceOrderId;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
        this.hiddenByAdminId = hiddenByAdminId;
        this.hiddenAt = hiddenAt;
    }

    // ===== Getter/Setter =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getServiceOrderId() { return serviceOrderId; }
    public void setServiceOrderId(Long serviceOrderId) { this.serviceOrderId = serviceOrderId; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isHidden() { return hidden; }
    public void setHidden(boolean hidden) { this.hidden = hidden; }

    public String getHiddenReason() { return hiddenReason; }
    public void setHiddenReason(String hiddenReason) { this.hiddenReason = hiddenReason; }

    public Long getHiddenByAdminId() { return hiddenByAdminId; }
    public void setHiddenByAdminId(Long hiddenByAdminId) { this.hiddenByAdminId = hiddenByAdminId; }

    public LocalDateTime getHiddenAt() { return hiddenAt; }
    public void setHiddenAt(LocalDateTime hiddenAt) { this.hiddenAt = hiddenAt; }
}
