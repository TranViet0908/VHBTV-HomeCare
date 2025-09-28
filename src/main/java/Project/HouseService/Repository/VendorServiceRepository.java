// src/main/java/Project/HouseService/Repository/VendorServiceRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
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

}
