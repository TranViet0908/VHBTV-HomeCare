// src/main/java/Project/HouseService/Repository/VendorServiceMediaRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorServiceMedia;
import Project.HouseService.Entity.VendorServiceMedia.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorServiceMediaRepository extends JpaRepository<VendorServiceMedia, Long> {
    List<VendorServiceMedia> findByVendorService_IdOrderBySortOrderAscIdAsc(Long vendorServiceId);
    int countByVendorService_Id(Long vendorServiceId);
    boolean existsByVendorService_IdAndCoverTrue(Long vendorServiceId);
    Optional<VendorServiceMedia> findFirstByVendorService_IdOrderBySortOrderAscIdAsc(Long vendorServiceId);

    @Modifying
    @Query("update VendorServiceMedia m set m.cover=false where m.vendorService.id=:vsId and m.cover=true")
    void clearCover(@Param("vsId") Long vendorServiceId);

    @Query("""
      select m from VendorServiceMedia m
      join m.vendorService vs
      where (:vendorId is null or vs.vendorId = :vendorId)
        and (:serviceId is null or vs.serviceId = :serviceId)
        and (:mediaType is null or m.mediaType = :mediaType)
        and (:isCover is null or m.cover = :isCover)
        and (
             :kw is null
             or lower(m.url) like lower(concat('%', :kw, '%'))
             or lower(coalesce(m.altText,'')) like lower(concat('%', :kw, '%'))
        )
      """)
    Page<VendorServiceMedia> search(
            @Param("vendorId") Long vendorId,
            @Param("serviceId") Long serviceId,
            @Param("mediaType") MediaType mediaType,
            @Param("isCover") Boolean isCover,
            @Param("kw") String kw,
            Pageable pageable
    );
}
