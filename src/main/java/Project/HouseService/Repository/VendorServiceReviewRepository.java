// src/main/java/Project/HouseService/Repository/VendorServiceReviewRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorServiceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VendorServiceReviewRepository extends JpaRepository<VendorServiceReview, Long> {

    @Query("""
      SELECT r FROM VendorServiceReview r
      WHERE (:kw IS NULL OR LOWER(r.content) LIKE LOWER(CONCAT('%', :kw, '%')))
        AND (:vendorId IS NULL OR r.vendorId = :vendorId)
        AND (:vendorServiceId IS NULL OR r.vendorServiceId = :vendorServiceId)
        AND (:hidden IS NULL OR r.hidden = :hidden)
      ORDER BY r.createdAt DESC
    """)
    Page<VendorServiceReview> search(@Param("kw") String keyword,
                                     @Param("vendorId") Long vendorId,
                                     @Param("vendorServiceId") Long vendorServiceId,
                                     @Param("hidden") Boolean hidden,
                                     Pageable pageable);

    boolean existsByServiceOrderItemId(Long serviceOrderItemId);
}
