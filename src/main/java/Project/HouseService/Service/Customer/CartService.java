// src/main/java/Project/HouseService/Service/Customer/CartService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.*;
import Project.HouseService.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final VendorServiceRepository vsRepo;
    private final VendorProfileRepository vendorProfileRepo;
    private final UserRepository userRepo;
    private final CouponRepository couponRepo;
    private final CouponServiceRepository couponServiceRepo;

    public CartService(CartRepository cartRepo,
                       CartItemRepository itemRepo,
                       VendorServiceRepository vsRepo,
                       VendorProfileRepository vendorProfileRepo,
                       UserRepository userRepo,
                       CouponRepository couponRepo,
                       CouponServiceRepository couponServiceRepo) {
        this.cartRepo = cartRepo;
        this.itemRepo = itemRepo;
        this.vsRepo = vsRepo;
        this.vendorProfileRepo = vendorProfileRepo;
        this.userRepo = userRepo;
        this.couponRepo = couponRepo;
        this.couponServiceRepo = couponServiceRepo;
    }

    private User mustGetUser(String username){
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User không tồn tại"));
    }

    private Cart getOrCreateActiveCart(User customer){
        return cartRepo.findByCustomer_IdAndStatus(customer.getId(), Cart.CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart c = new Cart();
                    c.setCustomer(customer);
                    c.setStatus(Cart.CartStatus.ACTIVE);
                    return cartRepo.save(c);
                });
    }

    @Transactional
    public Map<String,Object> addItem(String username,
                                      Long vendorServiceId,
                                      Integer quantity,
                                      LocalDateTime scheduleAt,
                                      String addressSnapshot,
                                      String notes){
        User customer = mustGetUser(username);
        if (quantity == null || quantity < 1) quantity = 1;

        VendorService vs = vsRepo.findById(vendorServiceId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy dịch vụ"));

        VendorProfile vendor = vendorProfileRepo.findByUser_Id(vs.getVendorId())
                .orElseThrow(() -> new NoSuchElementException("Vendor không tồn tại"));

        Cart cart = getOrCreateActiveCart(customer);

        Optional<CartItem> existed = itemRepo.findByCart_IdAndVendorService_Id(cart.getId(), vendorServiceId);
        CartItem it = existed.orElseGet(CartItem::new);
        if (it.getId() == null){
            it.setCart(cart);
            it.setVendor(vendor);
            it.setVendorService(vs);
            it.setQuantity(quantity);
        } else {
            it.setQuantity(it.getQuantity() + quantity);
        }

        long unitPrice = Optional.ofNullable(vs.getBasePrice()).orElse(BigDecimal.ZERO).longValue();
        it.setUnitPrice(unitPrice);
        it.setSubtotal(unitPrice * it.getQuantity());
        it.setScheduleAt(scheduleAt);
        it.setAddressSnapshot(addressSnapshot);
        it.setNotes(notes);

        itemRepo.save(it);
        return toCartPayload(cart, it.getId());
    }

    @Transactional
    public Map<String,Object> incItem(String username, Long itemId){
        User customer = mustGetUser(username);
        CartItem it = itemRepo.findByIdAndCart_Customer_Id(itemId, customer.getId())
                .orElseThrow(() -> new NoSuchElementException("Item không thuộc người dùng"));
        it.setQuantity(it.getQuantity() + 1);
        it.setSubtotal(it.getUnitPrice() * it.getQuantity());
        itemRepo.save(it);
        return toCartPayload(it.getCart(), it.getId());
    }

    @Transactional
    public Map<String,Object> decItem(String username, Long itemId){
        User customer = mustGetUser(username);
        CartItem it = itemRepo.findByIdAndCart_Customer_Id(itemId, customer.getId())
                .orElseThrow(() -> new NoSuchElementException("Item không thuộc người dùng"));
        int q = it.getQuantity() == null ? 1 : it.getQuantity();
        if (q <= 1){
            Cart cart = it.getCart();
            itemRepo.delete(it);
            return toCartPayload(cart, null);
        }
        it.setQuantity(q - 1);
        it.setSubtotal(it.getUnitPrice() * it.getQuantity());
        itemRepo.save(it);
        return toCartPayload(it.getCart(), it.getId());
    }

    @Transactional
    public Map<String,Object> removeItem(String username, Long itemId){
        User customer = mustGetUser(username);
        CartItem it = itemRepo.findByIdAndCart_Customer_Id(itemId, customer.getId())
                .orElseThrow(() -> new NoSuchElementException("Item không thuộc người dùng"));
        Cart cart = it.getCart();
        itemRepo.delete(it);
        return toCartPayload(cart, null);
    }

    @Transactional
    public Map<String,Object> applyCoupon(String username, String code){
        var customer = mustGetUser(username);
        var cart = cartRepo.findByCustomer_IdAndStatus(customer.getId(), Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Chưa có giỏ hàng"));

        var coupon = couponRepo.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new NoSuchElementException("Mã giảm giá không tồn tại"));

        if (!coupon.isActive()) throw new IllegalStateException("Mã giảm giá đã bị khóa");

        var now = LocalDateTime.now();
        if (coupon.getStartAt()!=null && now.isBefore(coupon.getStartAt())) throw new IllegalStateException("Mã giảm giá chưa bắt đầu");
        if (coupon.getEndAt()!=null && now.isAfter(coupon.getEndAt())) throw new IllegalStateException("Mã giảm giá đã hết hạn");

        // Thay thế mã hiện có (nếu có)
        cart.setCoupon(coupon);
        cartRepo.save(cart);
        return toCartPayload(cart, null);
    }

    @Transactional
    public Map<String,Object> removeCoupon(String username){
        var customer = mustGetUser(username);
        var cart = cartRepo.findByCustomer_IdAndStatus(customer.getId(), Cart.CartStatus.ACTIVE)
                .orElseThrow(() -> new NoSuchElementException("Chưa có giỏ hàng"));

        cart.setCoupon(null);
        cartRepo.save(cart);
        return toCartPayload(cart, null);
    }

    @Transactional(readOnly = true)
    public Map<String,Object> getCartJson(String username){
        User customer = userRepo.findByUsername(username).orElse(null);
        Cart cart = (customer == null) ? null :
                cartRepo.findByCustomer_IdAndStatus(customer.getId(), Cart.CartStatus.ACTIVE).orElse(null);
        if (cart == null){
            return Map.of("cartId", null, "status","ACTIVE", "items", List.of(), "total", 0L, "discount", 0L, "grandTotal", 0L, "count", 0);
        }
        return toCartPayload(cart, null);
    }

    private long computeDiscount(Cart cart, long total){
        var c = cart.getCoupon();
        if (c == null) return 0L;

        var cs = couponServiceRepo.findByCoupon_Id(c.getId());
        var allowServices = new HashSet<Long>();
        for (var x : cs) {
            if (x.getVendorService()!=null && x.getVendorService().getId()!=null) {
                allowServices.add(x.getVendorService().getId());
            }
        }
        boolean hasScopeList = !allowServices.isEmpty();

        long eligible = 0L;
        for (var ci : itemRepo.findByCart_Id(cart.getId())){
            Long sId = (ci.getVendorService()!=null) ? ci.getVendorService().getId() : null;
            boolean ok = !hasScopeList || (sId!=null && allowServices.contains(sId));
            if (ok) eligible += Optional.ofNullable(ci.getSubtotal()).orElse(0L);
        }
        if (eligible <= 0) return 0L;

        long discount;
        String type = (c.getType()==null) ? "FIXED" : c.getType().name();
        if ("PERCENT".equalsIgnoreCase(type)) {
            var pct = Optional.ofNullable(c.getValue()).orElse(BigDecimal.ZERO);
            discount = pct.multiply(BigDecimal.valueOf(eligible)).divide(BigDecimal.valueOf(100)).longValue();
            var cap = Optional.ofNullable(c.getMaxDiscountAmount()).orElse(BigDecimal.ZERO);
            if (cap.longValue()>0 && discount>cap.longValue()) discount = cap.longValue();
        } else {
            discount = Optional.ofNullable(c.getValue()).orElse(BigDecimal.ZERO).longValue();
            if (discount>eligible) discount = eligible;
        }
        if (discount<0) discount=0;
        if (discount>total) discount=total;
        return discount;
    }

    Map<String,Object> toCartPayload(Cart cart, Long addedItemId){
        List<CartItem> items = itemRepo.findByCart_Id(cart.getId());
        List<Map<String,Object>> arr = new ArrayList<>();
        long total = 0L;

        for (CartItem ci : items){
            total += Optional.ofNullable(ci.getSubtotal()).orElse(0L);

            VendorService vs = ci.getVendorService();
            VendorProfile vp = ci.getVendor();

            Long vendorUserId = null;
            String vendorName = "Nhà cung cấp";
            String vendorAvatar = null;

            if (vp != null) {
                vendorName = vp.getDisplayName() != null ? vp.getDisplayName() : "Nhà cung cấp";
                try {
                    User vendorUser = vp.getUser();
                    if (vendorUser != null) {
                        vendorUserId = vendorUser.getId();
                        vendorAvatar = vendorUser.getAvatarUrl();
                        if (vendorAvatar != null && !vendorAvatar.isBlank()) {
                            String v = vendorAvatar.trim();
                            if (!v.startsWith("http")) {
                                if (v.startsWith("/")) v = v.substring(1);
                                if (v.startsWith("uploads/")) v = v.substring("uploads/".length());
                                vendorAvatar = "/uploads/" + v;
                            }
                        } else {
                            vendorAvatar = null;
                        }
                    }
                } catch (Exception ignore) {}
            }

            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", ci.getId());
            m.put("vendorId", vendorUserId);
            m.put("vendorName", vendorName);
            m.put("vendorAvatar", vendorAvatar);

            m.put("vendorServiceId", (vs != null) ? vs.getId() : null);
            m.put("serviceTitle", (vs != null) ? vs.getTitle() : "Dịch vụ");
            m.put("serviceCover", (vs != null) ? vs.getCoverUrl() : null);
            m.put("durationMinutes", (vs != null) ? vs.getDurationMinutes() : 60);
            m.put("unit", (vs != null) ? vs.getUnit() : "lần");

            m.put("quantity", ci.getQuantity());
            m.put("unitPrice", ci.getUnitPrice());
            m.put("subtotal", ci.getSubtotal());
            m.put("scheduleAt", ci.getScheduleAt() == null ? null : ci.getScheduleAt().toString());
            m.put("address", ci.getAddressSnapshot());
            m.put("notes", ci.getNotes());

            arr.add(m);
        }

        long discount = computeDiscount(cart, total);
        long grand = total - discount;

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("cartId", cart.getId());
        out.put("status", cart.getStatus() != null ? cart.getStatus().name() : "ACTIVE");
        out.put("items", arr);
        out.put("total", total);
        out.put("discount", discount);
        out.put("grandTotal", grand);
        out.put("count", arr.size());
        if (cart.getCoupon() != null) {
            out.put("coupon", Map.of(
                    "code", cart.getCoupon().getCode(),
                    "name", cart.getCoupon().getName()
            ));
        }
        if (addedItemId != null) out.put("addedItemId", addedItemId);
        return out;
    }
}
