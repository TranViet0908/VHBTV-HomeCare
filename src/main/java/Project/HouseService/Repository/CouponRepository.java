// src/main/java/Project/HouseService/Repository/CouponRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.Coupon.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    Page<Coupon> findByCodeContainingIgnoreCase(String q, Pageable pageable);
    Page<Coupon> findByType(Type type, Pageable pageable);
    Page<Coupon> findByActive(boolean active, Pageable pageable);
    Page<Coupon> findByStartAtBeforeAndEndAtAfter(LocalDateTime now1, LocalDateTime now2, Pageable pageable);
    @Query("""
  select c from Coupon c
  where exists (
    select 1 from CouponVendor cv
    where cv.coupon.id = c.id and cv.vendor.id = :vendorId
  )
  and (:q is null or :q = '' 
       or lower(c.code) like lower(concat('%', :q, '%')) 
       or lower(c.name) like lower(concat('%', :q, '%')))
  order by c.id desc
""")
    Page<Coupon> findByVendor(@Param("vendorId") Long vendorId,
                              @Param("q") String q,
                              Pageable pageable);

}
