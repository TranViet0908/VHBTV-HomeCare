// src/main/java/Project/HouseService/Repository/ServiceOrderRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    Optional<ServiceOrder> findByIdAndVendorId(Long id, Long vendorId);
    Optional<ServiceOrder> findById(Long id);

    @Query("""
           SELECT o FROM ServiceOrder o
           WHERE o.vendorId = :vendorId
             AND (:status IS NULL OR o.status = :status)
             AND (:from IS NULL OR o.createdAt >= :from)
             AND (:to   IS NULL OR o.createdAt <  :to)
             AND (:q IS NULL OR LOWER(o.orderCode) LIKE LOWER(CONCAT('%',:q,'%'))
                              OR LOWER(o.contactName) LIKE LOWER(CONCAT('%',:q,'%'))
                              OR LOWER(o.contactPhone) LIKE LOWER(CONCAT('%',:q,'%')))
           """)
    Page<ServiceOrder> search(@Param("vendorId") Long vendorId,
                              @Param("status") String status,
                              @Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("q") String q,
                              Pageable pageable);
    Optional<ServiceOrder> findByOrderCode(String orderCode);

    @Query("""
        select so from ServiceOrder so
        where (:orderCode is null or lower(so.orderCode) like lower(concat('%', :orderCode, '%')))
          and (:status is null or so.status = :status)
          and (:vendorId is null or so.vendorId = :vendorId)
          and (:customerId is null or so.customerId = :customerId)
          and (:from is null or so.createdAt >= :from)
          and (:to is null or so.createdAt < :to)
          and (:minTotal is null or so.total >= :minTotal)
          and (:maxTotal is null or so.total <= :maxTotal)
          and (
               :hasCoupon is null
               or (:hasCoupon = true and so.couponId is not null)
               or (:hasCoupon = false and so.couponId is null)
          )
        """)
    Page<ServiceOrder> search(
            @Param("orderCode") String orderCode,
            @Param("status") String status,
            @Param("vendorId") Long vendorId,
            @Param("customerId") Long customerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal,
            @Param("hasCoupon") Boolean hasCoupon,
            Pageable pageable
    );
    @Query("""
      SELECT COUNT(o) > 0 FROM ServiceOrder o
      WHERE o.customerId = :customerId
        AND o.vendorId = :vendorId
        AND o.status = 'COMPLETED'
    """)
    boolean existsCompletedBetweenCustomerAndVendor(@Param("customerId") Long customerId,
                                                    @Param("vendorId") Long vendorId);

    @Query("select o.id, o.status from ServiceOrder o where o.id in :ids")
    List<Object[]> findIdAndStatusByIdIn(@Param("ids") List<Long> ids);

    @Query("""
                select coalesce(sum(o.subtotal),0),
                       coalesce(sum(o.discountAmount),0),
                       coalesce(sum(o.total),0)
                from ServiceOrder o
                where o.vendorId = :vendorId
                  and o.createdAt between :from and :to
            """)
    List<Object[]> sumOrderAmounts(@Param("vendorId") Long vendorId,
                                   @Param("from") LocalDateTime from,
                                   @Param("to") LocalDateTime to);


    @Query("""
                select o.status, count(o)
                from ServiceOrder o
                where o.vendorId = :vendorId
                  and o.createdAt between :from and :to
                group by o.status
            """)
    List<Object[]> countByOrderStatus(@Param("vendorId") Long vendorId,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to);
}
