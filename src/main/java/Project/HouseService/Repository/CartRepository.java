// src/main/java/Project/HouseService/Repository/CartRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer_IdAndStatus(Long customerId, Cart.CartStatus status);

    Optional<Cart> findByIdAndCustomer_Id(Long id, Long customerId);
}
