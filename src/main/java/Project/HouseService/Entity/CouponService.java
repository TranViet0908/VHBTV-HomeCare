// src/main/java/Project/HouseService/Entity/CouponService.java
package Project.HouseService.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "coupon_service",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_coupon_service",
                        columnNames = {"coupon_id","vendor_service_id"})
        })
public class CouponService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> coupon.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_csvc_coupon"))
    private Coupon coupon;

    // FK -> vendor_service.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendor_service_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_csvc_vs"))
    private VendorService vendorService;

    public CouponService() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }

    public VendorService getVendorService() { return vendorService; }
    public void setVendorService(VendorService vendorService) { this.vendorService = vendorService; }
}
