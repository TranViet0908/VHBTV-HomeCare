// src/main/java/Project/HouseService/Repository/CouponRedemptionRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CouponRedemption;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRedemptionRepository extends JpaRepository<CouponRedemption, Long> {
    long countByCoupon_Id(Long couponId);
    long countByCoupon_IdAndUser_Id(Long couponId, Long userId);
    Page<CouponRedemption> findByCoupon_Id(Long couponId, Pageable pageable);
}
