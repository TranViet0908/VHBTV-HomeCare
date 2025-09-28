// src/main/java/Project/HouseService/Repository/CouponRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.Coupon.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    Page<Coupon> findByCodeContainingIgnoreCase(String q, Pageable pageable);
    Page<Coupon> findByType(Type type, Pageable pageable);
    Page<Coupon> findByActive(boolean active, Pageable pageable);
    Page<Coupon> findByStartAtBeforeAndEndAtAfter(LocalDateTime now1, LocalDateTime now2, Pageable pageable);
}
