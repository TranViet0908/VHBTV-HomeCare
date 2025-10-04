// src/main/java/Project/HouseService/Entity/CustomerEvent.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_event")
public class CustomerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "session_id", nullable = false, length = 64)
    private String sessionId;

    // Tham chiếu FK nhưng giữ kiểu Long để tránh join nặng
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "service_id")
    private Long serviceId;

    // vendor_id là user_id của vendor_profile
    @Column(name = "vendor_id")
    private Long vendorId;

    @Column(name = "vendor_service_id")
    private Long vendorServiceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private Action action;

    @Column(name = "meta", columnDefinition = "text")
    private String meta;

    public CustomerEvent() {}

    // Enum viết ngay trong file entity theo yêu cầu
    public enum Action {
        VIEW_SERVICE,
        VIEW_VENDOR,
        VIEW_VENDOR_LIST,
        VIEW_VENDOR_SERVICE,
        CLICK_VENDOR,
        CLICK_VENDOR_SERVICE,
        ADD_WISHLIST,
        BEGIN_CHECKOUT,
        PURCHASE
    }

    // Getters/Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getOccurredAt() { return occurredAt; }
    public void setOccurredAt(LocalDateTime occurredAt) { this.occurredAt = occurredAt; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }

    public Long getVendorServiceId() { return vendorServiceId; }
    public void setVendorServiceId(Long vendorServiceId) { this.vendorServiceId = vendorServiceId; }

    public Action getAction() { return action; }
    public void setAction(Action action) { this.action = action; }

    public String getMeta() { return meta; }
    public void setMeta(String meta) { this.meta = meta; }
}
