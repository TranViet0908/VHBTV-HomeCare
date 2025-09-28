// src/main/java/Project/HouseService/Repository/CouponVendorRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CouponVendor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CouponVendorRepository extends JpaRepository<CouponVendor, Long> {
    List<CouponVendor> findByCoupon_Id(Long couponId);
    boolean existsByCoupon_IdAndVendor_Id(Long couponId, Long vendorId);
    void deleteByCoupon_IdAndVendor_Id(Long couponId, Long vendorId);
    long countByCoupon_Id(Long couponId);
}
