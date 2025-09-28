package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface VendorSkillRepository extends JpaRepository<VendorSkill, Long> {

    Page<VendorSkill> findByVendorId(Long vendorId, Pageable pageable);

    boolean existsByVendorIdAndSlug(Long vendorId, String slug);

    @Query("""
           SELECT v FROM VendorSkill v
           WHERE (:vendorId IS NULL OR v.vendorId = :vendorId)
             AND (
                  :q IS NULL OR
                  LOWER(v.name) LIKE LOWER(CONCAT('%', :q, '%')) OR
                  LOWER(v.slug) LIKE LOWER(CONCAT('%', :q, '%'))
             )
           """)
    Page<VendorSkill> search(@Param("vendorId") Long vendorId,
                             @Param("q") String q,
                             Pageable pageable);
}
