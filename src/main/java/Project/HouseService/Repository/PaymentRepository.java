// src/main/java/Project/HouseService/Repository/PaymentRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.Payment.PayTargetType;
import Project.HouseService.Entity.Payment.PaymentStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByPayTargetTypeAndPayTargetId(PayTargetType type, Long id);

    @Query("""
        select coalesce(sum(p.amount), 0) 
        from Payment p 
        where p.payTargetType = :type and p.payTargetId = :id and p.status = Project.HouseService.Entity.Payment.PaymentStatus.PAID
    """)
    BigDecimal sumPaid(@Param("type") PayTargetType type, @Param("id") Long id);

    boolean existsByPayTargetTypeAndPayTargetIdAndStatus(PayTargetType type, Long id, PaymentStatus status);
}
