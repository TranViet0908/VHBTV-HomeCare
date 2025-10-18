// src/main/java/Project/HouseService/Repository/PaymentRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.Payment.PayTargetType;
import Project.HouseService.Entity.Payment.PaymentStatus;
import Project.HouseService.Entity.Payment.Provider;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPayTargetTypeAndPayTargetId(PayTargetType type, Long id);
    List<Payment> findByPayTargetTypeAndPayTargetIdOrderByPaidAtDesc(PayTargetType payTargetType,
                                                                     Long payTargetId);

    Optional<Payment> findFirstByPayTargetTypeAndPayTargetIdOrderByPaidAtDesc(PayTargetType payTargetType,
                                                                              Long payTargetId);
    @Query("""
        select coalesce(sum(p.amount), 0) 
        from Payment p 
        where p.payTargetType = :type and p.payTargetId = :id and p.status = Project.HouseService.Entity.Payment.PaymentStatus.PAID
    """)
    BigDecimal sumPaid(@Param("type") PayTargetType type, @Param("id") Long id);

    boolean existsByPayTargetTypeAndPayTargetIdAndStatus(PayTargetType type, Long id, PaymentStatus status);

    // ===== Thêm cho Vendor =====

    @Query("""
        select coalesce(sum(p.amount), 0)
        from Payment p
        join ServiceOrder o on o.id = p.payTargetId
        where p.payTargetType = Project.HouseService.Entity.Payment.PayTargetType.SERVICE_ORDER
          and o.vendorId = :vendorId
          and p.status = Project.HouseService.Entity.Payment.PaymentStatus.PAID
          and p.paidAt between :from and :to
    """)
    BigDecimal totalPaidForVendor(@Param("vendorId") Long vendorId,
                                  @Param("from") LocalDateTime from,
                                  @Param("to") LocalDateTime to);

    @Query("""
        select p.provider, coalesce(sum(p.amount),0)
        from Payment p
        join ServiceOrder o on o.id = p.payTargetId
        where p.payTargetType = Project.HouseService.Entity.Payment.PayTargetType.SERVICE_ORDER
          and o.vendorId = :vendorId
          and p.status = Project.HouseService.Entity.Payment.PaymentStatus.PAID
          and p.paidAt between :from and :to
        group by p.provider
        order by sum(p.amount) desc
    """)
    List<Object[]> sumByProviderForVendor(@Param("vendorId") Long vendorId,
                                          @Param("from") LocalDateTime from,
                                          @Param("to") LocalDateTime to);

    @Query("""
        select p.status, coalesce(sum(p.amount),0)
        from Payment p
        join ServiceOrder o on o.id = p.payTargetId
        where p.payTargetType = Project.HouseService.Entity.Payment.PayTargetType.SERVICE_ORDER
          and o.vendorId = :vendorId
          and p.paidAt between :from and :to
        group by p.status
    """)
    List<Object[]> sumByStatusForVendor(@Param("vendorId") Long vendorId,
                                        @Param("from") LocalDateTime from,
                                        @Param("to") LocalDateTime to);

    @Query("""
        select function('date', p.paidAt) as d, coalesce(sum(p.amount),0)
        from Payment p
        join ServiceOrder o on o.id = p.payTargetId
        where p.payTargetType = Project.HouseService.Entity.Payment.PayTargetType.SERVICE_ORDER
          and o.vendorId = :vendorId
          and p.status = Project.HouseService.Entity.Payment.PaymentStatus.PAID
          and p.paidAt between :from and :to
        group by function('date', p.paidAt)
        order by d
    """)
    List<Object[]> dailyPaidSeriesForVendor(@Param("vendorId") Long vendorId,
                                            @Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to);

    @Query("""
        select p
        from Payment p
        join ServiceOrder o on o.id = p.payTargetId
        where p.payTargetType = Project.HouseService.Entity.Payment.PayTargetType.SERVICE_ORDER
          and o.vendorId = :vendorId
          and p.paidAt between :from and :to
          and (:provider is null or p.provider = :provider)
          and (:status   is null or p.status   = :status)
        order by p.paidAt desc, p.id desc
    """)
    List<Payment> findByVendorBetween(@Param("vendorId") Long vendorId,
                                      @Param("from") LocalDateTime from,
                                      @Param("to") LocalDateTime to,
                                      @Param("provider") Provider provider,
                                      @Param("status") PaymentStatus status);
    Optional<Payment> findByTransactionRef(String transactionRef);

    @Query("""
        select p from Payment p
        where p.payTargetType = :payTargetType
          and p.payTargetId   = :payTargetId
          and p.provider      = :provider
          and p.status        = :status
        order by p.id desc
    """)
    List<Payment> findLatestByTargetAndStatus(@Param("payTargetType") PayTargetType payTargetType,
                                              @Param("payTargetId") Long payTargetId,
                                              @Param("provider") Provider provider,
                                              @Param("status") PaymentStatus status);
}
