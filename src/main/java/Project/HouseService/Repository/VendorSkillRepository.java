package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorSkill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorSkillRepository extends JpaRepository<VendorSkill, Long> {

    Page<VendorSkill> findByVendorId(Long vendorId, Pageable pageable);

    boolean existsByVendorIdAndSlug(Long vendorId, String slug);
    // thêm cho module vendor
    Optional<VendorSkill> findByIdAndVendorId(Long id, Long vendorId);

    // tiện cho trang danh sách
    List<VendorSkill> findByVendorIdOrderByNameAsc(Long vendorId);
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
