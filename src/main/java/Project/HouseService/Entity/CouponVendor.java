// src/main/java/Project/HouseService/Entity/CouponVendor.java
package Project.HouseService.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "coupon_vendor",
        uniqueConstraints = @UniqueConstraint(name="uk_coupon_vendor", columnNames = {"coupon_id","vendor_id"}))
public class CouponVendor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="coupon_id", nullable=false)
    private Coupon coupon;

    public CouponVendor(){}

    public CouponVendor(Long id, Coupon coupon, User vendor) {
        this.id = id;
        this.coupon = coupon;
        this.vendor = vendor;
    }

    // vendor_id = user.id của VENDOR
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="vendor_id", nullable=false)



    private User vendor;

    public Long getId() { return id; }
    public Coupon getCoupon() { return coupon; }
    public User getVendor() { return vendor; }
    public void setId(Long id) { this.id = id; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public void setVendor(User vendor) { this.vendor = vendor; }
}
