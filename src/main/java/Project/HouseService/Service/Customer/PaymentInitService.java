// src/main/java/Project/HouseService/Service/Customer/PaymentInitService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.Payment.PayTargetType;
import Project.HouseService.Entity.Payment.Provider;
import Project.HouseService.Entity.Payment.PaymentStatus;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentInitService {

    private final PaymentRepository paymentRepository;

    public PaymentInitService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public void initForOrders(Long userId, List<ServiceOrder> orders, Provider provider) {
        for (ServiceOrder so : orders) {
            Payment p = new Payment();
            p.setUserId(userId);
            p.setPayTargetType(PayTargetType.SERVICE_ORDER);
            p.setPayTargetId(so.getId());
            p.setProvider(provider);
            p.setAmount(so.getTotal() == null ? BigDecimal.ZERO : so.getTotal());
            p.setCurrency("VND");
            p.setStatus(PaymentStatus.PENDING);
            p.setTransactionRef("SRV-" + so.getOrderCode());
            p.setPaidAt(null);
            // createdAt/updatedAt nếu entity có @PrePersist/@PreUpdate sẽ tự set
            paymentRepository.save(p);
        }
    }
}
