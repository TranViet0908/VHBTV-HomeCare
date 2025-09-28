// src/main/java/Project/HouseService/Service/Admin/ServiceOrderAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Repository.PaymentRepository;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceOrderAdminService {

    private final ServiceOrderRepository orders;
    private final ServiceOrderItemRepository items;
    private final PaymentRepository payments;

    public ServiceOrderAdminService(ServiceOrderRepository orders,
                                    ServiceOrderItemRepository items,
                                    PaymentRepository payments) {
        this.orders = orders;
        this.items = items;
        this.payments = payments;
    }

    public Page<ServiceOrder> search(String orderCode,
                                     String status,
                                     Long vendorId,
                                     Long customerId,
                                     LocalDateTime from,
                                     LocalDateTime to,
                                     BigDecimal minTotal,
                                     BigDecimal maxTotal,
                                     Boolean hasCoupon,
                                     Pageable pageable) {
        String st = (status == null || status.isBlank()) ? null : status.trim().toUpperCase();
        return orders.search(orderCode, st, vendorId, customerId, from, to, minTotal, maxTotal, hasCoupon, pageable);
    }

    public ServiceOrder getOrThrow(Long id) {
        return orders.findById(id).orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại: " + id));
    }

    public List<ServiceOrderItem> getItems(Long orderId) {
        return items.findByServiceOrderId(orderId);
    }

    public List<Payment> getPayments(Long orderId) {
        return payments.findByPayTargetTypeAndPayTargetId(Payment.PayTargetType.SERVICE_ORDER, orderId);
    }

    @Transactional
    public void updateContact(Long orderId, String name, String phone, String address, String notes) {
        ServiceOrder so = getOrThrow(orderId);
        so.setContactName(name);
        so.setContactPhone(phone);
        so.setAddressLine(address);
        so.setNotes(notes);
        orders.save(so);
    }

    @Transactional
    public void changeStatus(Long orderId, String newStatus) {
        ServiceOrder so = getOrThrow(orderId);
        String ns = (newStatus == null) ? null : newStatus.trim().toUpperCase();
        if (ns == null || ns.isEmpty()) throw new IllegalArgumentException("Thiếu trạng thái mới");
        switch (ns) {
            case "PENDING":
            case "CONFIRMED":
            case "COMPLETED":
            case "CANCELLED":
                break;
            default:
                throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);
        }

        if ("COMPLETED".equals(ns)) {
            boolean hasPending = payments.existsByPayTargetTypeAndPayTargetIdAndStatus(Payment.PayTargetType.SERVICE_ORDER, orderId, Payment.PaymentStatus.PENDING);
            boolean hasFailed  = payments.existsByPayTargetTypeAndPayTargetIdAndStatus(Payment.PayTargetType.SERVICE_ORDER, orderId, Payment.PaymentStatus.FAILED);
            if (hasPending || hasFailed) throw new IllegalStateException("Không thể hoàn tất khi còn thanh toán PENDING/FAILED.");
        }
        if ("CANCELLED".equals(ns)) {
            boolean hasPaid = payments.existsByPayTargetTypeAndPayTargetIdAndStatus(Payment.PayTargetType.SERVICE_ORDER, orderId, Payment.PaymentStatus.PAID);
            if (hasPaid) throw new IllegalStateException("Không thể hủy khi đã có thanh toán PAID chưa hoàn tiền.");
        }

        so.setStatus(ns); // entity đang dùng String
        orders.save(so);
    }

    @Transactional
    public void recalcTotals(Long orderId) {
        ServiceOrder so = getOrThrow(orderId);
        List<ServiceOrderItem> list = items.findByServiceOrderId(orderId);
        BigDecimal subtotal = BigDecimal.ZERO;
        for (ServiceOrderItem it : list) {
            if (it != null && it.getSubtotal() != null) subtotal = subtotal.add(it.getSubtotal());
        }
        so.setSubtotal(subtotal);
        BigDecimal discount = so.getDiscountAmount() == null ? BigDecimal.ZERO : so.getDiscountAmount();
        BigDecimal total = subtotal.subtract(discount);
        if (total.signum() < 0) total = BigDecimal.ZERO;
        so.setTotal(total);
        orders.save(so);
    }
}
