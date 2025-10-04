// src/main/java/Project/HouseService/Repository/ServiceRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    Optional<Service> findBySlug(String slug);
    boolean existsBySlug(String slug);

    // Vì dùng trường phẳng
    boolean existsByParentId(Long parentId);
    List<Service> findAllByOrderByNameAsc();

    @Query("""
        select s from Service s
        where (:kw is null or :kw = '' 
               or lower(s.name) like lower(concat('%', :kw, '%'))
               or lower(s.slug) like lower(concat('%', :kw, '%'))
               or lower(coalesce(s.description,'')) like lower(concat('%', :kw, '%'))
               or lower(coalesce(s.unit,'')) like lower(concat('%', :kw, '%')))
          and (:parentId is null or s.parentId = :parentId)
          and (:unit is null or :unit = '' or lower(coalesce(s.unit,'')) = lower(:unit))
        """)
    Page<Service> search(@Param("kw") String kw,
                         @Param("parentId") Long parentId,
                         @Param("unit") String unit,
                         Pageable pageable);
    // Lấy các dịch vụ chuẩn mà vendor CHƯA có
    @Query(
            value = """
              SELECT s.* FROM service s
              WHERE s.id NOT IN (
                 SELECT vs.service_id FROM vendor_service vs WHERE vs.vendor_id = :vendorId
              )
              ORDER BY s.name
              """,
            nativeQuery = true
    )
    List<Service> findAssignableForVendor(@Param("vendorId") Long vendorId);

    // Lấy nhóm cha
    List<Service> findByParentIdIsNullOrderByNameAsc();

    // Lấy con theo cha
    List<Service> findByParentIdOrderByNameAsc(Long parentId);

    // Tìm nhóm cha theo tên chứa (insensitive) — dùng LOWER để phù hợp dữ liệu
    @Query("SELECT s FROM Service s WHERE s.parentId IS NULL AND LOWER(s.name) LIKE LOWER(:kw) ORDER BY s.name ASC")
    List<Service> findRootByNameLike(@Param("kw") String keyword);

    // Tìm con theo cha và tên chứa
    @Query("SELECT s FROM Service s WHERE s.parentId = :pid AND LOWER(s.name) LIKE LOWER(:kw) ORDER BY s.name ASC")
    List<Service> findChildrenByParentIdAndNameLike(@Param("pid") Long parentId, @Param("kw") String keyword);

    // Bổ sung cho suggestions
    List<Service> findByIdInOrderByNameAsc(List<Long> ids);

}
