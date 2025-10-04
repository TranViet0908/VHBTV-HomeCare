// src/main/java/Project/HouseService/Repository/VendorServiceReviewRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorServiceReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface VendorServiceReviewRepository extends JpaRepository<VendorServiceReview, Long> {

    // ===== GIỮ NGUYÊN =====
    boolean existsByServiceOrderItemId(Long serviceOrderItemId);

    @Query(value = """
            SELECT vs.service_id AS service_id,
                   COALESCE(AVG(r.rating), 0) AS avg_rating,
                   COUNT(r.id) AS rating_count
            FROM vendor_service_review r
            JOIN vendor_service vs ON vs.id = r.vendor_service_id
            WHERE r.hidden = 0 AND vs.service_id IN (:ids)
            GROUP BY vs.service_id
            """, nativeQuery = true)
    List<Map<String, Object>> ratingAggByServiceIds(@Param("ids") Collection<Long> serviceIds);

    // Tìm kiếm cho Admin (GIỮ NGUYÊN theo yêu cầu)
    @Query(value = """
        select r
        from VendorServiceReview r, VendorService vs, User cu
        where vs.id = r.vendorServiceId
          and cu.id = r.customerId
          and (:kw is null or :kw = '' 
               or lower(coalesce(r.content,'')) like lower(concat('%', :kw, '%'))
               or lower(coalesce(cu.username,'')) like lower(concat('%', :kw, '%')))
          and (:vendorId is null or vs.vendorId = :vendorId)
          and (:vendorServiceId is null or r.vendorServiceId = :vendorServiceId)
          and (:hidden is null or r.hidden = :hidden)
        order by r.createdAt desc
        """,
            countQuery = """
        select count(r)
        from VendorServiceReview r, VendorService vs, User cu
        where vs.id = r.vendorServiceId
          and cu.id = r.customerId
          and (:kw is null or :kw = '' 
               or lower(coalesce(r.content,'')) like lower(concat('%', :kw, '%'))
               or lower(coalesce(cu.username,'')) like lower(concat('%', :kw, '%')))
          and (:vendorId is null or vs.vendorId = :vendorId)
          and (:vendorServiceId is null or r.vendorServiceId = :vendorServiceId)
          and (:hidden is null or r.hidden = :hidden)
        """)
    Page<VendorServiceReview> search(@Param("kw") String kw,
                                     @Param("vendorId") Long vendorId,
                                     @Param("vendorServiceId") Long vendorServiceId,
                                     @Param("hidden") Boolean hidden,
                                     Pageable pageable);

    // ===== THÊM MỚI: cho trang chi tiết dịch vụ (review hiển thị = hidden=false hoặc null) =====

    Page<VendorServiceReview> findByVendorServiceIdAndHiddenFalseOrderByCreatedAtDesc(
            Long vendorServiceId, Pageable pageable);

    // Đếm review HIỂN THỊ cho gói dịch vụ cụ thể
    @Query("""
        select count(r) from VendorServiceReview r
        where r.vendorServiceId = :vsId and (r.hidden = false or r.hidden is null)
    """)
    long countVisibleByVendorServiceId(@Param("vsId") Long vendorServiceId);

    // Trung bình sao HIỂN THỊ cho gói dịch vụ cụ thể
    @Query("""
        select coalesce(avg(r.rating), 0)
        from VendorServiceReview r
        where r.vendorServiceId = :vsId and (r.hidden = false or r.hidden is null)
    """)
    Double avgByVendorServiceId(@Param("vsId") Long vendorServiceId);

    // Đếm review HIỂN THỊ cho VENDOR (gom mọi gói)
    @Query("""
        select count(r)
        from VendorServiceReview r, VendorService vs
        where r.vendorServiceId = vs.id
          and vs.vendorId = :vendorId
          and (r.hidden = false or r.hidden is null)
    """)
    long countByVendorIdVisible(@Param("vendorId") Long vendorId);

    // Trung bình sao HIỂN THỊ cho VENDOR (gom mọi gói)
    @Query("""
        select coalesce(avg(r.rating), 0)
        from VendorServiceReview r, VendorService vs
        where r.vendorServiceId = vs.id
          and vs.vendorId = :vendorId
          and (r.hidden = false or r.hidden is null)
    """)
    Double avgByVendorIdVisible(@Param("vendorId") Long vendorId);
}
