// src/main/java/Project/HouseService/Repository/CouponUserRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CouponUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponUserRepository extends JpaRepository<CouponUser, Long> {
    List<CouponUser> findByCoupon_Id(Long couponId);
    boolean existsByCoupon_IdAndUser_Id(Long couponId, Long userId);
    long countByCoupon_Id(Long couponId);
    void deleteByCoupon_IdAndUser_Id(Long couponId, Long userId);

}
