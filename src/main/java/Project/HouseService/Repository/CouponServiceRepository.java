// src/main/java/Project/HouseService/Repository/CouponServiceRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CouponService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponServiceRepository extends JpaRepository<CouponService, Long> {
    List<CouponService> findByCoupon_Id(Long couponId);
    boolean existsByCoupon_IdAndVendorService_Id(Long couponId, Long vendorServiceId);
}
