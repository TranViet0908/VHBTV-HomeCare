// src/main/java/Project/HouseService/Repository/VendorReviewRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VendorReviewRepository extends JpaRepository<VendorReview, Long> {

    @Query("""
      SELECT r FROM VendorReview r
      WHERE (:kw IS NULL OR LOWER(r.content) LIKE LOWER(CONCAT('%', :kw, '%')))
        AND (:vendorId IS NULL OR r.vendorId = :vendorId)
        AND (:hidden IS NULL OR r.hidden = :hidden)
      ORDER BY r.createdAt DESC
    """)
    Page<VendorReview> search(@Param("kw") String keyword,
                              @Param("vendorId") Long vendorId,
                              @Param("hidden") Boolean hidden,
                              Pageable pageable);

    @Query("""
      SELECT COALESCE(AVG(r.rating),0), COUNT(r)
      FROM VendorReview r
      WHERE r.vendorId = :vendorId AND r.hidden = false AND r.rating IS NOT NULL
    """)
    Object[] avgAndCountForVendor(@Param("vendorId") Long vendorId);

    boolean existsByServiceOrderId(Long serviceOrderId);
    // Đếm review HIỂN THỊ cho VENDOR (hidden=false hoặc null)
    @Query("""
        select count(vr)
        from VendorReview vr
        where vr.vendorId = :vendorId
          and (vr.hidden = false or vr.hidden is null)
    """)
    long countVisibleByVendorId(@Param("vendorId") Long vendorId);

    // Trung bình sao HIỂN THỊ cho VENDOR
    @Query("""
        select coalesce(avg(vr.rating), 0)
        from VendorReview vr
        where vr.vendorId = :vendorId
          and (vr.hidden = false or vr.hidden is null)
    """)
    Double avgByVendorIdVisible(@Param("vendorId") Long vendorId);

    @Query(value = "SELECT COUNT(*) FROM vendor_review WHERE vendor_id = :vendorId", nativeQuery = true)
    Long countByVendorIdNative(@Param("vendorId") Long vendorId);

    @Query(value = "SELECT COALESCE(AVG(rating),0) FROM vendor_review WHERE vendor_id = :vendorId", nativeQuery = true)
    Double avgRatingByVendorId(@Param("vendorId") Long vendorId);

    @org.springframework.data.jpa.repository.Query(value = """
    SELECT vr.vendor_id       AS vendor_id,
           AVG(vr.rating)     AS avg_rating,
           COUNT(vr.id)       AS cnt
    FROM vendor_review vr
    WHERE vr.vendor_id IN (:ids)
    GROUP BY vr.vendor_id
""", nativeQuery = true)
    java.util.List<Object[]> aggRatingByVendorIds(
            @org.springframework.data.repository.query.Param("ids") java.util.Collection<Long> ids);
}
