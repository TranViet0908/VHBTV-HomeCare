// src/main/java/Project/HouseService/Repository/VendorApplicationRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Entity.VendorApplication.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VendorApplicationRepository extends JpaRepository<VendorApplication, Long> {
    Optional<VendorApplication> findByUser_IdAndStatus(Long userId, Status status);
    boolean existsByUser_IdAndStatus(Long userId, Status status);
    Page<VendorApplication> findAllByStatus(Status status, Pageable pageable);
}
