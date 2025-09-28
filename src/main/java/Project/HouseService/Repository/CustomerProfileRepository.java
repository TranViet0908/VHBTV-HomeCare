// src/main/java/Project/HouseService/Repository/CustomerProfileRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CustomerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByUser_Id(Long userId);
    boolean existsByUser_Id(Long userId);
}
