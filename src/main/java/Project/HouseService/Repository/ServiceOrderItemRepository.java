// src/main/java/Project/HouseService/Repository/ServiceOrderItemRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.ServiceOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceOrderItemRepository extends JpaRepository<ServiceOrderItem, Long> {
    List<ServiceOrderItem> findByServiceOrderId(Long serviceOrderId);
    @Query("""
      SELECT COUNT(i) > 0
      FROM ServiceOrderItem i
      JOIN ServiceOrder o ON o.id = i.serviceOrderId
      WHERE o.customerId = :customerId
        AND i.id = :serviceOrderItemId
        AND o.status = 'COMPLETED'
    """)
    boolean existsCompletedForCustomerAndItem(@Param("customerId") Long customerId,
                                              @Param("serviceOrderItemId") Long serviceOrderItemId);
}
