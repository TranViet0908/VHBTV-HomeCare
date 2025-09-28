package Project.HouseService.Entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon", indexes = {
        @Index(name = "ix_coupon_code", columnList = "code", unique = true),
        @Index(name = "ix_coupon_active", columnList = "is_active"),
        @Index(name = "ix_coupon_time", columnList = "start_at,end_at")
})
public class Coupon {
    public enum Type { PERCENT, FIXED }
    public enum Scope { SERVICE } // chỉ còn SERVICE

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String code;

    @Column(nullable = false, length = 200)
    private String name; // << thêm

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'SERVICE'")
    private Scope scope = Scope.SERVICE; // mặc định SERVICE

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value; // % hoặc số tiền

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount; // null/0 = không giới hạn

    @Column(name = "usage_limit_global")
    private Integer usageLimitGlobal; // null/0 = không giới hạn

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser; // null/0 = không giới hạn

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "start_at")
    private LocalDateTime startAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        if (scope == null) scope = Scope.SERVICE;      // chống null -> lỗi enum
        if (name == null || name.isBlank()) name = code; // fallback
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (updatedAt == null) updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        if (name == null || name.isBlank()) name = code; // đảm bảo không null
        this.updatedAt = LocalDateTime.now();
    }

    public Coupon(){}

    public Coupon(Long id, String code, String name, Type type, Scope scope, BigDecimal value, BigDecimal maxDiscountAmount,
                  Integer usageLimitGlobal, Integer usageLimitPerUser, LocalDateTime startAt, LocalDateTime endAt,
                  boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.value = value;
        this.maxDiscountAmount = maxDiscountAmount;
        this.usageLimitGlobal = usageLimitGlobal;
        this.usageLimitPerUser = usageLimitPerUser;
        this.startAt = startAt;
        this.endAt = endAt;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public void setMaxDiscountAmount(BigDecimal maxDiscountAmount) {
        this.maxDiscountAmount = maxDiscountAmount;
    }

    public Integer getUsageLimitGlobal() {
        return usageLimitGlobal;
    }

    public void setUsageLimitGlobal(Integer usageLimitGlobal) {
        this.usageLimitGlobal = usageLimitGlobal;
    }

    public Integer getUsageLimitPerUser() {
        return usageLimitPerUser;
    }

    public void setUsageLimitPerUser(Integer usageLimitPerUser) {
        this.usageLimitPerUser = usageLimitPerUser;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
