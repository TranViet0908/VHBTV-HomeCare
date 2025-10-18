package Project.HouseService.Repository;

import Project.HouseService.Entity.WishlistService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistServiceRepository extends JpaRepository<WishlistService, Long> {

    boolean existsByCustomer_IdAndVendorService_Id(Long customerId, Long vendorServiceId);

    void deleteByCustomer_IdAndVendorService_Id(Long customerId, Long vendorServiceId);

    long countByCustomer_Id(Long customerId);

    List<WishlistService> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    Page<WishlistService> findByCustomer_Id(Long customerId, Pageable pageable);
}
