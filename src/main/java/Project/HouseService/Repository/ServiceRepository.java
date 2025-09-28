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
}
