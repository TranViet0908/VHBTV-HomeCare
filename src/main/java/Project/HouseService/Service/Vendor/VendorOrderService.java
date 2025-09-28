// src/main/java/Project/HouseService/Service/Vendor/VendorOrderService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import Project.HouseService.Repository.UserRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
public class VendorOrderService {

    private final ServiceOrderRepository orders;
    private final ServiceOrderItemRepository items;
    private final UserRepository users;

    public VendorOrderService(ServiceOrderRepository orders,
                              ServiceOrderItemRepository items,
                              UserRepository users) {
        this.orders = orders; this.items = items; this.users = users;
    }

    public Long currentVendorId(String username) {
        return users.findByUsername(username).orElseThrow().getId(); // vendor_id = user.id
    }

    public Page<ServiceOrder> list(Long vendorId, String status, String q,
                                   LocalDate from, LocalDate to, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1),
                Sort.by(Sort.Direction.DESC,"createdAt"));
        LocalDateTime fromDT = from == null ? null : from.atStartOfDay();
        LocalDateTime toDT   = to   == null ? null : to.plusDays(1).atStartOfDay();
        String kw = (q == null || q.isBlank()) ? null : q.trim();
        return orders.search(vendorId, norm(status), fromDT, toDT, kw, pageable);
    }

    public ServiceOrder get(Long vendorId, Long id) {
        return orders.findByIdAndVendorId(id, vendorId).orElseThrow(NoSuchElementException::new);
    }

    public List<ServiceOrderItem> items(Long orderId) {
        return items.findByServiceOrderId(orderId);
    }

    public void transition(Long vendorId, Long id, String action) {
        ServiceOrder o = get(vendorId, id);
        String cur = norm(o.getStatus());
        String act = norm(action);
        String next = switch (act) {
            case "CONFIRM"  -> cur.equals("PENDING")     ? "CONFIRMED"   : fail();
            case "START"    -> cur.equals("CONFIRMED")   ? "IN_PROGRESS" : fail();
            case "COMPLETE" -> cur.equals("IN_PROGRESS") ? "COMPLETED"   : fail();
            case "CANCEL"   -> cur.equals("COMPLETED")   ? fail()        : "CANCELLED";
            default -> fail();
        };
        o.setStatus(next);
    }

    private String norm(String s){
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t.toUpperCase();
    }

    private String fail(){ throw new IllegalStateException("Trạng thái không hợp lệ"); }
}
