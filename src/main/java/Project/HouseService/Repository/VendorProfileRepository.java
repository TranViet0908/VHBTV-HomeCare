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
}
