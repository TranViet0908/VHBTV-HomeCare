// src/main/java/Project/HouseService/Repository/CustomerEventRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.CustomerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerEventRepository extends JpaRepository<CustomerEvent, Long> {

    // Điểm cá nhân theo service dựa trên vendor_service_id (sự kiện gắn vào VS)
    @Query(value = """
        SELECT vs.service_id AS service_id,
               SUM(
                 (CASE e.action
                    WHEN 'PURCHASE' THEN 5
                    WHEN 'BEGIN_CHECKOUT' THEN 4
                    WHEN 'ADD_WISHLIST' THEN 4
                    WHEN 'CLICK_VENDOR_SERVICE' THEN 3
                    WHEN 'CLICK_VENDOR' THEN 2
                    WHEN 'VIEW_VENDOR_SERVICE' THEN 2
                    WHEN 'VIEW_VENDOR' THEN 1
                    WHEN 'VIEW_SERVICE' THEN 1
                    ELSE 0 END)
                 * EXP(-:lambda * TIMESTAMPDIFF(DAY, e.occurred_at, NOW()))
               ) AS score
        FROM customer_event e
        JOIN vendor_service vs ON vs.id = e.vendor_service_id
        WHERE e.customer_id = :customerId
          AND e.occurred_at >= :since
        GROUP BY vs.service_id
        """, nativeQuery = true)
    List<Object[]> scoreByVendorServiceEvents(@Param("customerId") Long customerId,
                                              @Param("since") LocalDateTime since,
                                              @Param("lambda") double lambda);

    // Điểm cá nhân theo service từ sự kiện gắn trực tiếp service_id
    @Query(value = """
        SELECT e.service_id AS service_id,
               SUM(
                 (CASE e.action
                    WHEN 'PURCHASE' THEN 5
                    WHEN 'BEGIN_CHECKOUT' THEN 4
                    WHEN 'ADD_WISHLIST' THEN 4
                    WHEN 'CLICK_VENDOR_SERVICE' THEN 3
                    WHEN 'CLICK_VENDOR' THEN 2
                    WHEN 'VIEW_VENDOR_SERVICE' THEN 2
                    WHEN 'VIEW_VENDOR' THEN 1
                    WHEN 'VIEW_SERVICE' THEN 1
                    ELSE 0 END)
                 * EXP(-:lambda * TIMESTAMPDIFF(DAY, e.occurred_at, NOW()))
               ) AS score
        FROM customer_event e
        WHERE e.customer_id = :customerId
          AND e.service_id IS NOT NULL
          AND e.occurred_at >= :since
        GROUP BY e.service_id
        """, nativeQuery = true)
    List<Object[]> scoreByServiceEvents(@Param("customerId") Long customerId,
                                        @Param("since") LocalDateTime since,
                                        @Param("lambda") double lambda);

    // Lấy vendor vừa click theo session để ưu tiên xếp hạng runtime
    @Query(value = """
        SELECT DISTINCT e.vendor_id
        FROM customer_event e
        WHERE e.session_id = :sessionId
          AND e.vendor_id IS NOT NULL
          AND e.action IN ('CLICK_VENDOR','CLICK_VENDOR_SERVICE')
          AND e.occurred_at >= :since
        ORDER BY MAX(e.occurred_at) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Long> recentClickedVendorsBySession(@Param("sessionId") String sessionId,
                                             @Param("since") LocalDateTime since,
                                             @Param("limit") int limit);
}
