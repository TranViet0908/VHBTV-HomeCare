// src/main/java/Project/HouseService/Service/Customer/OrderService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Controller.Customer.CheckoutController.Contact;
import Project.HouseService.Controller.Customer.CheckoutController.Item;
import Project.HouseService.Entity.*;
import Project.HouseService.Repository.CouponRedemptionRepository;
import Project.HouseService.Repository.CouponRepository;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    public static class CreateResult {
        public final List<ServiceOrder> orders;
        public CreateResult(List<ServiceOrder> orders) { this.orders = orders; }
    }

    private final ServiceOrderRepository orderRepo;
    private final ServiceOrderItemRepository itemRepo;
    private final CouponRedemptionRepository redemptionRepo;
    private final CouponRepository couponRepository;

    public OrderService(ServiceOrderRepository orderRepo,
                        ServiceOrderItemRepository itemRepo,
                        CouponRedemptionRepository redemptionRepo,
                        CouponRepository couponRepository) {
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
        this.redemptionRepo = redemptionRepo;
        this.couponRepository = couponRepository;
    }

    @Transactional
    public CreateResult createOrdersFromCheckout(User user,
                                                 List<Item> items,
                                                 Contact contact,
                                                 String couponCode,
                                                 Map<Long, BigDecimal> discountAllocByVendor) {

        // 0) Resolve couponId từ couponCode (nếu có)
        Long couponId = null;
        if (couponCode != null && !couponCode.isBlank()) {
            couponId = couponRepository.findByCodeIgnoreCase(couponCode.trim())
                    .map(Coupon::getId)
                    .orElse(null);
        }

        // Nhóm theo vendor
        Map<Long, List<Item>> byVendor = new LinkedHashMap<>();
        for (Item it : items) {
            byVendor.computeIfAbsent(it.vendorId, k -> new ArrayList<>()).add(it);
        }

        List<ServiceOrder> created = new ArrayList<>();
        for (Map.Entry<Long, List<Item>> e : byVendor.entrySet()) {
            Long vendorId = e.getKey();
            List<Item> vItems = e.getValue();

            BigDecimal subtotal = vItems.stream()
                    .map(it -> it.subtotal == null ? BigDecimal.ZERO : it.subtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discount = discountAllocByVendor == null
                    ? BigDecimal.ZERO
                    : discountAllocByVendor.getOrDefault(vendorId, BigDecimal.ZERO);

            BigDecimal total = subtotal.subtract(discount);

            ServiceOrder so = new ServiceOrder();
            so.setOrderCode(generateCode());
            so.setCustomerId(user.getId());
            so.setVendorId(vendorId);
            so.setStatus("PENDING");
            so.setSubtotal(subtotal);
            so.setDiscountAmount(discount);
            so.setTotal(total);
            so.setContactName(contact == null ? null : contact.contactName);
            so.setContactPhone(contact == null ? null : contact.contactPhone);
            so.setAddressLine(contact == null ? null : contact.addressLine);
            so.setNotes(contact == null ? null : contact.notes);
            so.setCreatedAt(LocalDateTime.now());
            so.setUpdatedAt(LocalDateTime.now());

            // GẮN couponId cho order nếu có
            if (couponId != null) {
                so.setCouponId(couponId);
            }

            orderRepo.save(so);

            // Items
            for (Item it : vItems) {
                ServiceOrderItem oi = new ServiceOrderItem();
                oi.setServiceOrderId(so.getId());
                oi.setVendorId(vendorId);
                oi.setVendorService(it.vendorService);
                oi.setScheduledAt(it.scheduledAt);
                oi.setQuantity(it.quantity);
                oi.setUnitPrice(it.unitPrice);
                oi.setSubtotal(it.subtotal);
                itemRepo.save(oi);
            }

            created.add(so);
        }

        // BỎ HOÀN TOÀN việc ghi coupon_redemption ở đây
        return new CreateResult(created);
    }

    private String generateCode() {
        String base = java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String code = "SO" + base + (int)(Math.random()*9000 + 1000);
        // đảm bảo unique thô sơ; có thể loop nếu cần
        return code;
    }
}
