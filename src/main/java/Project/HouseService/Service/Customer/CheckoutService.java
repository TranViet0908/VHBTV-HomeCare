// src/main/java/Project/HouseService/Service/Customer/CheckoutService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Controller.Customer.CheckoutController.Item;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class CheckoutService {

    @PersistenceContext
    private EntityManager em;

    private final VendorServiceRepository vendorServiceRepository;
    private final VendorProfileRepository vendorProfileRepository;

    public CheckoutService(VendorServiceRepository vendorServiceRepository,
                           VendorProfileRepository vendorProfileRepository) {
        this.vendorServiceRepository = vendorServiceRepository;
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @Transactional(readOnly = true)
    public void fillCartState(Project.HouseService.Controller.Customer.CheckoutController.CheckoutState state,
                              Long userId) {
        // Lấy cart ACTIVE của user và coupon kèm theo
        Long cartId = null;
        String couponCode = null;
        List<?> cartRow = em.createNativeQuery(
                        "select id, coupon_id from carts where customer_id=:uid and status='ACTIVE' order by id desc limit 1")
                .setParameter("uid", userId).getResultList();
        if (!cartRow.isEmpty()) {
            Object[] r0 = (Object[]) cartRow.get(0);
            cartId = ((Number) r0[0]).longValue();
            Number couponId = (Number) r0[1];
            if (couponId != null) {
                List<?> codeRow = em.createNativeQuery("select code from coupon where id=:cid")
                        .setParameter("cid", couponId.longValue()).getResultList();
                if (!codeRow.isEmpty()) couponCode = String.valueOf(codeRow.get(0));
            }
        }

        // THÊM cột address_snapshot để lấy địa chỉ người dùng đã nhập trong giỏ
        String sql = """
        select ci.id, ci.vendor_service_id, ci.quantity, ci.schedule_at, ci.notes,
               ci.vendor_id, ci.unit_price, ci.subtotal, ci.address_snapshot
        from cart_items ci
        where ci.cart_id = :cartId
        order by ci.id asc
    """;
        state.items.clear();
        if (cartId == null) {
            state.couponCode = couponCode; // null nếu không có
            return;
        }
        Query q = em.createNativeQuery(sql);
        q.setParameter("cartId", cartId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();

        List<Long> vendorServiceIds = new ArrayList<>();
        Set<Long> vendorIds = new HashSet<>();
        for (Object[] r : rows) {
            Long vsId = toLong(r[1]);
            if (vsId != null) vendorServiceIds.add(vsId);
            Long vId = toLong(r[5]);
            if (vId != null) vendorIds.add(vId);
        }

        Map<Long, VendorService> vsMap = new HashMap<>();
        for (VendorService vs : vendorServiceRepository.findAllById(vendorServiceIds)) {
            if (vs != null) vsMap.put(vs.getId(), vs);
        }
        Map<Long, VendorProfile> vpMap = new HashMap<>();
        for (VendorProfile vp : vendorProfileRepository.findByUserIdIn(vendorIds)) {
            if (vp != null && vp.getUser() != null) {
                vpMap.put(vp.getUser().getId(), vp);
            }
        }

        for (Object[] r : rows) {
            Long id = toLong(r[0]);
            Long vsId = toLong(r[1]);
            Integer qty = toInt(r[2], 1);
            LocalDateTime sched = toDateTime(r[3]);
            String notes = r[4] == null ? null : String.valueOf(r[4]);
            Long vId = toLong(r[5]);
            BigDecimal unitPrice = r[6] == null ? null : new BigDecimal(r[6].toString());
            BigDecimal subtotal = r[7] == null ? null : new BigDecimal(r[7].toString());
            String addressSnapshot = r[8] == null ? null : String.valueOf(r[8]); // <— địa chỉ từ giỏ

            VendorService vs = vsMap.get(vsId);
            if (vs == null) continue;

            Item it = new Item();
            it.itemId = id;
            it.vendorService = vs;
            it.vendorId = vId != null ? vId : vs.getVendorId();
            it.vendor = vpMap.get(it.vendorId);
            it.quantity = (qty == null || qty < 1) ? 1 : qty;
            it.unitPrice = unitPrice != null ? unitPrice : vs.getBasePrice();
            it.subtotal = subtotal != null ? subtotal : it.unitPrice.multiply(BigDecimal.valueOf(it.quantity));
            it.scheduledAt = sched;
            it.notes = notes;
            // GÁN địa chỉ để form confirm gửi lên và OrderService ghi vào service_order.address_line
            it.addressLine = (addressSnapshot != null && !addressSnapshot.isBlank()) ? addressSnapshot.trim() : null;

            state.items.add(it);
        }

        // Ưu tiên coupon của giỏ nếu có
        state.couponCode = (couponCode != null && !couponCode.isBlank()) ? couponCode : state.couponCode;
    }

    @Transactional(readOnly = true)
    public void fillBuyNowState(Project.HouseService.Controller.Customer.CheckoutController.CheckoutState state,
                                Long userId, Long vendorServiceId, Integer quantity, String scheduleAtIso) {
        state.items.clear();

        VendorService vs = vendorServiceRepository.findById(vendorServiceId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy dịch vụ: " + vendorServiceId));

        VendorProfile vp = null;
        for (VendorProfile v : vendorProfileRepository.findByUserIdIn(List.of(vs.getVendorId()))) {
            vp = v; break;
        }

        Item it = new Item();
        it.itemId = null;
        it.vendorService = vs;
        it.vendorId = vs.getVendorId();
        it.vendor = vp;
        it.quantity = (quantity == null || quantity < 1) ? 1 : quantity;
        it.unitPrice = vs.getBasePrice();
        it.subtotal = it.unitPrice.multiply(java.math.BigDecimal.valueOf(it.quantity));
        it.scheduledAt = parseIso(scheduleAtIso);
        it.notes = null;
        it.addressLine = null; // địa chỉ sẽ nhập ở bước tiếp theo

        state.items.add(it);
    }

    @Transactional
    public void clearCartItems(Long userId, List<Item> items) {
        Set<Long> vsIds = new HashSet<>();
        for (Item it : items) if (it.vendorService != null) vsIds.add(it.vendorService.getId());
        if (vsIds.isEmpty()) return;

        // Xóa theo cart ACTIVE của user
        String sql = """
            delete ci from cart_items ci
            where ci.cart_id = (select id from carts where customer_id=:uid and status='ACTIVE' order by id desc limit 1)
              and ci.vendor_service_id in (:ids)
        """;
        em.createNativeQuery(sql)
                .setParameter("uid", userId)
                .setParameter("ids", vsIds)
                .executeUpdate();
    }

    private static Long toLong(Object x) { return x == null ? null : ((Number)x).longValue(); }
    private static Integer toInt(Object x, int def) { return x == null ? def : ((Number)x).intValue(); }
    private static LocalDateTime toDateTime(Object x) {
        if (x == null) return null;
        if (x instanceof java.sql.Timestamp t) return t.toLocalDateTime();
        if (x instanceof LocalDateTime ldt) return ldt;
        return null;
    }
    private static LocalDateTime parseIso(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try { return LocalDateTime.parse(iso); } catch (DateTimeParseException e) { return null; }
    }
}
