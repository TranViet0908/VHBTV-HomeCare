// src/main/java/Project/HouseService/Service/Admin/ServiceOrderItemAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ServiceOrderItemAdminService {

    private final ServiceOrderRepository orders;
    private final ServiceOrderItemRepository items;
    private final ServiceOrderAdminService orderLogic;
    private final VendorServiceRepository vendorServices;

    public ServiceOrderItemAdminService(ServiceOrderRepository orders,
                                        ServiceOrderItemRepository items,
                                        ServiceOrderAdminService orderLogic,
                                        VendorServiceRepository vendorServices) {
        this.orders = orders;
        this.items = items;
        this.orderLogic = orderLogic;
        this.vendorServices = vendorServices;
    }

    public List<ServiceOrderItem> list(Long orderId) {
        return items.findByServiceOrderId(orderId);
    }

    @Transactional
    public ServiceOrderItem create(Long orderId, Long vendorId, Long vendorServiceId,
                                   LocalDateTime scheduledAt, Integer quantity, BigDecimal unitPrice) {
        ServiceOrder so = orders.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại: " + orderId));

        VendorService vs = vendorServices.findById(vendorServiceId)
                .orElseThrow(() -> new IllegalArgumentException("VendorService không tồn tại: " + vendorServiceId));

        ServiceOrderItem it = new ServiceOrderItem();
        it.setServiceOrderId(so.getId());
        it.setVendorId(vendorId);
        it.setVendorService(vs); // <-- gán đúng kiểu entity

        it.setScheduledAt(scheduledAt);
        it.setQuantity(quantity);
        it.setUnitPrice(unitPrice);

        BigDecimal price = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        int qty = quantity == null ? 0 : quantity;
        it.setSubtotal(price.multiply(BigDecimal.valueOf(qty)));

        ServiceOrderItem saved = items.save(it);
        orderLogic.recalcTotals(orderId);
        return saved;
    }

    @Transactional
    public void update(Long itemId, LocalDateTime scheduledAt, Integer quantity, BigDecimal unitPrice) {
        ServiceOrderItem it = items.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item không tồn tại: " + itemId));

        it.setScheduledAt(scheduledAt);
        it.setQuantity(quantity);
        it.setUnitPrice(unitPrice);

        BigDecimal price = unitPrice == null ? BigDecimal.ZERO : unitPrice;
        int qty = quantity == null ? 0 : quantity;
        it.setSubtotal(price.multiply(BigDecimal.valueOf(qty)));

        items.save(it);
        orderLogic.recalcTotals(it.getServiceOrderId());
    }

    @Transactional
    public void delete(Long itemId) {
        ServiceOrderItem it = items.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item không tồn tại: " + itemId));
        Long orderId = it.getServiceOrderId();
        items.delete(it);
        orderLogic.recalcTotals(orderId);
    }
}
