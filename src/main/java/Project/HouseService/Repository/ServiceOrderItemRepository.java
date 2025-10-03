// src/main/java/Project/HouseService/Repository/ServiceOrderItemRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.ServiceOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
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

    // Lấy tất cả item của 1 vendor trong khoảng thời gian
    List<ServiceOrderItem> findByVendorIdAndScheduledAtBetweenOrderByScheduledAtAsc(
            Long vendorId, LocalDateTime from, LocalDateTime to
    );

    List<ServiceOrderItem> findByVendorIdInAndScheduledAtBetweenOrderByScheduledAtAsc(
            List<Long> vendorIds, LocalDateTime from, LocalDateTime to);

    // Đếm số item trong 1 ngày/khoảng
    long countByVendorIdAndScheduledAtBetween(Long vendorId, LocalDateTime from, LocalDateTime to);

    // Kiểm tra có item nằm trong cửa sổ thời gian hay không (dùng khi sinh slot)
    boolean existsByVendorIdAndScheduledAtBetween(Long vendorId, LocalDateTime start, LocalDateTime end);

    // Lọc theo trạng thái order hợp lệ (tránh tính CANCELLED/REJECTED)
    @Query("""
               select i from ServiceOrderItem i
               join ServiceOrder o on o.id = i.serviceOrderId
               where i.vendorId = :vendorId
                 and i.scheduledAt between :from and :to
                 and o.status in :statuses
               order by i.scheduledAt asc
            """)
    List<ServiceOrderItem> findBookedByVendorBetweenWithStatuses(@Param("vendorId") Long vendorId,
                                                                 @Param("from") LocalDateTime from,
                                                                 @Param("to") LocalDateTime to,
                                                                 @Param("statuses") Collection<String> statuses);

    // Kiểm tra “đã có booking trùng khung” theo danh sách trạng thái hợp lệ
    @Query("""
               select count(i) > 0 from ServiceOrderItem i
               join ServiceOrder o on o.id = i.serviceOrderId
               where i.vendorId = :vendorId
                 and i.scheduledAt >= :start and i.scheduledAt < :end
                 and o.status in :statuses
            """)
    boolean existsOverlapForVendorWithStatuses(@Param("vendorId") Long vendorId,
                                               @Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               @Param("statuses") Collection<String> statuses);

    // Trả về (scheduledAt, status) để đếm theo ngày, không cần DTO
    @Query("""
               select i.scheduledAt, o.status
               from ServiceOrderItem i
               join ServiceOrder o on o.id = i.serviceOrderId
               where i.vendorId = :vendorId
                 and i.scheduledAt between :from and :to
            """)
    List<Object[]> findScheduleStatusByVendorBetween(@Param("vendorId") Long vendorId,
                                                     @Param("from") LocalDateTime from,
                                                     @Param("to") LocalDateTime to);

    // Smart: lấy (ngày dùng cho lịch, trạng thái)
    @Query("""
            select i.scheduledAt as atTime,
                    o.status
                    from ServiceOrderItem i
                    join ServiceOrder o on o.id = i.serviceOrderId
                    where (i.vendorId = :vendorId or o.vendorId = :vendorId)
                    and i.scheduledAt between :from and :to
            """)
    List<Object[]> findScheduleStatusSmartByVendorBetween(@Param("vendorId") Long vendorId,
                                                          @Param("from") LocalDateTime from,
                                                          @Param("to") LocalDateTime to);

    @Query(value = """
                select s.name as service_name,
                       sum(i.subtotal) as revenue,
                       sum(i.quantity) as qty
                from service_order_item i
                join vendor_service vs on vs.id = i.vendor_service_id
                join service s on s.id = vs.service_id
                join service_order o on o.id = i.service_order_id
                where (i.vendor_id = :vendorId or o.vendor_id = :vendorId)
                  and o.created_at between :from and :to
                group by s.name
                order by revenue desc
                limit :limit
            """, nativeQuery = true)
    List<Object[]> topServicesByRevenue(@Param("vendorId") Long vendorId,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to,
                                        @Param("limit") int limit);

}
