// src/main/java/Project/HouseService/Repository/VendorProfileRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VendorProfileRepository extends JpaRepository<VendorProfile, Long> {
    // đúng cú pháp property traversal
    Optional<VendorProfile> findByUser_Username(String username);
    Optional<VendorProfile> findByUser_Id(Long userId);

    @Query("select vp.id from VendorProfile vp where vp.user.id = :userId")
    Optional<Long> findVendorIdByUserId(@Param("userId") Long userId);

    @Query("""
           select vp
           from VendorProfile vp
           left join vp.user u
           where (:verified is null or vp.verified = :verified)
             and (
                  :kw is null
                  or lower(u.username) like lower(concat('%', :kw, '%'))
                  or lower(coalesce(vp.displayName, '')) like lower(concat('%', :kw, '%'))
                  or lower(coalesce(vp.legalName,  '')) like lower(concat('%', :kw, '%'))
                 )
           """)
    Page<VendorProfile> search(@Param("kw") String kw,
                               @Param("verified") Boolean verified,
                               Pageable pageable);

    // load kèm User để render tên vendor trong select
    @Query("select vp from VendorProfile vp join fetch vp.user u")
    List<VendorProfile> findAllWithUser();
    @Query("SELECT v FROM VendorProfile v WHERE v.user.id = :userId")
    VendorProfile findByUserId(@Param("userId") Long userId);

    // Fallback theo username từ SecurityContext
    @Query("SELECT v FROM VendorProfile v WHERE v.user.username = :username")
    VendorProfile findByUserUsername(@Param("username") String username);

    // Fallback theo email từ SecurityContext
    @Query("SELECT v FROM VendorProfile v WHERE v.user.email = :email")
    VendorProfile findByUserEmail(@Param("email") String email);

    Optional<VendorProfile> findFirstByDisplayNameIgnoreCase(String displayName);

    Optional<VendorProfile> findByUser_UsernameIgnoreCase(String username);

    @Query("select vp from VendorProfile vp " +
            "where lower(function('replace', vp.displayName, ' ', '-')) = lower(:slug)")
    Optional<VendorProfile> findByDisplayNameSlugIgnoreCase(@Param("slug") String slug);

    Optional<VendorProfile> findByDisplayNameIgnoreCase(String displayName);

    @Query("""
              select v from VendorProfile v
              where (:q is null or :q = '' or lower(v.displayName) like lower(concat('%', :q, '%')))
                and (:verified is null or v.verified = :verified)
                and (:ratingMin is null or coalesce(v.ratingAvg,0) >= :ratingMin)
                and (:yearsMin is null or coalesce(v.yearsExperience,0) >= :yearsMin)
              order by v.id desc
            """)
    Page<Project.HouseService.Entity.VendorProfile> search(
            @Param("q") String q,
            @Param("verified") Boolean verified,
            @Param("ratingMin") java.math.BigDecimal ratingMin,
            @Param("yearsMin") Integer yearsMin,
            Pageable pageable);

    @Query("select v from VendorProfile v where v.user.id in :ids")
    List<Project.HouseService.Entity.VendorProfile> findByUserIdIn(@Param("ids") java.util.Collection<Long> ids);

    @Query("select v from VendorProfile v where v.verified = true order by coalesce(v.ratingAvg,0) desc, coalesce(v.ratingCount,0) desc")
    List<Project.HouseService.Entity.VendorProfile> topVerified(Pageable pageable);
}
