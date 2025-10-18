package Project.HouseService.Repository;

import Project.HouseService.Entity.WishlistVendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistVendorRepository extends JpaRepository<WishlistVendor, Long> {

    boolean existsByCustomer_IdAndVendor_Id(Long customerId, Long vendorId);

    void deleteByCustomer_IdAndVendor_Id(Long customerId, Long vendorId);

    long countByCustomer_Id(Long customerId);

    List<WishlistVendor> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    Page<WishlistVendor> findByCustomer_Id(Long customerId, Pageable pageable);
}
