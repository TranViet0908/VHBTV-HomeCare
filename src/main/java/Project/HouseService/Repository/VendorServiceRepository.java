// src/main/java/Project/HouseService/Repository/VendorServiceRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface VendorServiceRepository extends JpaRepository<VendorService, Long> {

    boolean existsByVendorIdAndServiceIdAndTitleIgnoreCase(Long vendorId, Long serviceId, String title);

    @Query("""
           SELECT v FROM VendorService v
           WHERE (:kw IS NULL OR
                 LOWER(CAST(v.title AS string)) LIKE CONCAT('%', LOWER(:kw), '%')
              OR LOWER(CAST(v.description AS string)) LIKE CONCAT('%', LOWER(:kw), '%'))
             AND (:vendorId IS NULL OR v.vendorId = :vendorId)
             AND (:serviceId IS NULL OR v.serviceId = :serviceId)
             AND (:status IS NULL OR UPPER(v.status) = UPPER(:status))
             AND (:minPrice IS NULL OR v.basePrice >= :minPrice)
             AND (:maxPrice IS NULL OR v.basePrice <= :maxPrice)
           ORDER BY v.updatedAt DESC
           """)
    Page<VendorService> search(@Param("kw") String kw,
                               @Param("vendorId") Long vendorId,
                               @Param("serviceId") Long serviceId,
                               @Param("status") String status,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               Pageable pageable);

    Optional<VendorService> findById(Long id);
    long countByServiceId(Long serviceId);
    List<VendorService> findAllByOrderByTitleAsc();
    List<VendorService> findByVendorIdAndStatusOrderByTitleAsc(Long vendorId, String status);

    Optional<VendorService> findByVendorIdAndId(Long vendorId, Long id);
    List<VendorService> findByVendorIdOrderByUpdatedAtDesc(Long vendorId);
    boolean existsByVendorIdAndServiceId(Long vendorId, Long serviceId);
    // thêm
    Optional<VendorService> findByIdAndVendorId(Long id, Long vendorId);
    long deleteByIdAndVendorId(Long id, Long vendorId);
    List<VendorService> findByVendorIdOrderByTitleAsc(Long vendorId);
    // Đếm số vendor_service ACTIVE theo service_id
    @Query(value = """
            SELECT vs.service_id AS service_id, COUNT(*) AS cnt
            FROM vendor_service vs
            WHERE vs.status = 'ACTIVE' AND vs.service_id IN (:ids)
            GROUP BY vs.service_id
            """, nativeQuery = true)
    List<Map<String, Object>> countActiveByServiceIds(@Param("ids") Collection<Long> serviceIds);

    // Min base_price theo service_id
    @Query(value = """
            SELECT vs.service_id AS service_id, MIN(vs.base_price) AS min_price
            FROM vendor_service vs
            WHERE vs.status = 'ACTIVE' AND vs.service_id IN (:ids)
            GROUP BY vs.service_id
            """, nativeQuery = true)
    List<Map<String, Object>> minPriceByServiceIds(@Param("ids") Collection<Long> serviceIds);

    // Avg duration + Min notice theo service_id
    @Query(value = """
            SELECT vs.service_id AS service_id,
                   AVG(vs.duration_minutes) AS avg_duration,
                   MIN(vs.min_notice_hours) AS min_notice
            FROM vendor_service vs
            WHERE vs.status = 'ACTIVE' AND vs.service_id IN (:ids)
            GROUP BY vs.service_id
            """, nativeQuery = true)
    List<Map<String, Object>> durationAndNoticeByServiceIds(@Param("ids") Collection<Long> serviceIds);

    @Query(value = """
    SELECT vendor_service_id AS vsId, COUNT(*) AS cnt
    FROM service_order_item
    WHERE vendor_service_id IN (:ids)
    GROUP BY vendor_service_id
    """, nativeQuery = true)
    List<Map<String,Object>> countOrdersByVendorServiceIds(@Param("ids") Collection<Long> ids);
}
