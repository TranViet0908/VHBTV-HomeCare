// src/main/java/Project/HouseService/Entity/CouponUser.java
package Project.HouseService.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "coupon_user",
        uniqueConstraints = @UniqueConstraint(name="uk_coupon_user", columnNames = {"coupon_id","user_id"}))
public class CouponUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="coupon_id", nullable=false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    public CouponUser(){

    }

    public CouponUser(Long id, Coupon coupon, User user) {
        this.id = id;
        this.coupon = coupon;
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
