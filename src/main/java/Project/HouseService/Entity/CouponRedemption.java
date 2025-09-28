// src/main/java/Project/HouseService/Entity/CouponRedemption.java
package Project.HouseService.Entity;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "coupon_redemption", indexes = {
        @Index(name="ix_redemption_coupon", columnList = "coupon_id"),
        @Index(name="ix_redemption_user", columnList = "user_id"),
        @Index(name="ix_redemption_service", columnList = "vendor_service_id"),
        @Index(name="ix_redemption_time", columnList = "redeemed_at")
})
public class CouponRedemption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // áp cho đơn dịch vụ
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="coupon_id", nullable=false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user; // có thể null nếu không gắn user

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="vendor_service_id")
    private VendorService vendorService; // có thể null nếu không gắn dịch vụ cụ thể

    @Column(name="amount_discounted", precision = 12, scale = 2, nullable=false)
    private BigDecimal amountDiscounted;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @Column(name="redeemed_at", nullable=false)
    private LocalDateTime redeemedAt = LocalDateTime.now();

    public CouponRedemption(){

    }

    public CouponRedemption(Long id, Coupon coupon, User user, VendorService vendorService, BigDecimal amountDiscounted, LocalDateTime redeemedAt) {
        this.id = id;
        this.coupon = coupon;
        this.user = user;
        this.vendorService = vendorService;
        this.amountDiscounted = amountDiscounted;
        this.redeemedAt = redeemedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VendorService getVendorService() {
        return vendorService;
    }

    public void setVendorService(VendorService vendorService) {
        this.vendorService = vendorService;
    }

    public BigDecimal getAmountDiscounted() {
        return amountDiscounted;
    }

    public void setAmountDiscounted(BigDecimal amountDiscounted) {
        this.amountDiscounted = amountDiscounted;
    }

    public LocalDateTime getRedeemedAt() {
        return redeemedAt;
    }

    public void setRedeemedAt(LocalDateTime redeemedAt) {
        this.redeemedAt = redeemedAt;
    }
}
