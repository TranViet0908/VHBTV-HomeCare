// src/main/java/Project/HouseService/Entity/VendorService.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "vendor_service")
public class VendorService {

    public enum DisplayStatus { ACTIVE, HIDDEN, PAUSED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> vendor_profile.user_id
    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    // FK -> service.id
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "unit", length = 50)
    private String unit = "job";

    @Column(name = "duration_minutes")
    private Integer durationMinutes = 60;

    @Column(name = "min_notice_hours")
    private Integer minNoticeHours = 24;

    @Column(name = "max_daily_jobs")
    private Integer maxDailyJobs = 10;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    // Lưu String để đơn giản
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    @Column(name = "created_at")  private LocalDateTime createdAt;
    @Column(name = "updated_at")  private LocalDateTime updatedAt;

    public VendorService(){

    }

    public VendorService(Long id, Long vendorId, Long serviceId, String title, String description, BigDecimal basePrice, String unit, Integer durationMinutes, Integer minNoticeHours, Integer maxDailyJobs, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.vendorId = vendorId;
        this.serviceId = serviceId;
        this.title = title;
        this.description = description;
        this.basePrice = basePrice;
        this.unit = unit;
        this.durationMinutes = durationMinutes;
        this.minNoticeHours = minNoticeHours;
        this.maxDailyJobs = maxDailyJobs;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters
    public Long getId() { return id; }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getServiceId() { return serviceId; }
    public void setServiceId(Long serviceId) { this.serviceId = serviceId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public Integer getMinNoticeHours() { return minNoticeHours; }
    public void setMinNoticeHours(Integer minNoticeHours) { this.minNoticeHours = minNoticeHours; }
    public Integer getMaxDailyJobs() { return maxDailyJobs; }
    public void setMaxDailyJobs(Integer maxDailyJobs) { this.maxDailyJobs = maxDailyJobs; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
