// src/main/java/Project/HouseService/Repository/CartItemRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCart_Id(Long cartId);

    Optional<CartItem> findByCart_IdAndVendorService_Id(Long cartId, Long vendorServiceId);
    Optional<CartItem> findByIdAndCart_Customer_Id(Long id, Long customerId);

}
