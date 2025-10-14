// src/main/java/Project/HouseService/Controller/Customer/CheckoutController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.CouponRepository;
import Project.HouseService.Repository.CustomerProfileRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Service.Customer.CheckoutService;
import Project.HouseService.Service.Customer.CouponCalcService;
import Project.HouseService.Service.Customer.OrderService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequestMapping("/customer/checkout")
@SessionAttributes("checkout")
public class CheckoutController {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private CouponRepository couponRepository;

    // removed: not used here
    // @Autowired private CouponRedemptionRepository couponRedemptionRepository;
    // @Autowired private ServiceOrderItemRepository serviceOrderItemRepository;

    private final CheckoutService checkoutService;
    private final CouponCalcService couponCalcService;
    private final OrderService orderService;
    // private final PaymentInitService paymentInitService; // removed
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;

    public CheckoutController(CheckoutService checkoutService,
                              CouponCalcService couponCalcService,
                              OrderService orderService,
                              UserRepository userRepository,
                              CustomerProfileRepository customerProfileRepository,
                              CouponRepository couponRepository) {
        this.checkoutService = checkoutService;
        this.couponCalcService = couponCalcService;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.couponRepository = couponRepository;
    }

    public static class CheckoutState {
        public String source;
        public List<Item> items = new ArrayList<>();
        public Contact contact = new Contact();
        public String couponCode;
        public BigDecimal subtotal = BigDecimal.ZERO;
        public BigDecimal discountAmount = BigDecimal.ZERO;
        public BigDecimal total = BigDecimal.ZERO;
        public Map<Long, BigDecimal> discountAllocByVendor = new HashMap<>();
    }

    public static class Contact {
        public String contactName;
        public String contactPhone;
        public String addressLine;
        public String provinceId;
        public String districtId;
        public String notes;
    }

    public static class Item {
        public Long itemId;
        public Long vendorId;
        public VendorService vendorService;
        public Integer quantity;
        public LocalDateTime scheduledAt;
        public String notes;
        public String addressLine;
        public BigDecimal unitPrice;
        public BigDecimal subtotal;
        public VendorProfile vendor;
    }

    public static class VendorOrderView {
        public VendorProfile vendor;
        public String vendorAvatarUrl;
        public List<Item> items = new ArrayList<>();
        public BigDecimal subtotal = BigDecimal.ZERO;
        public BigDecimal discount = BigDecimal.ZERO;
        public BigDecimal total = BigDecimal.ZERO;
    }

    private String resolveVendorAvatar(Project.HouseService.Entity.VendorProfile vp) {
        if (vp == null) return null;
        for (String mName : new String[]{"getAvatarUrl", "getAvatar", "getLogoUrl", "getImageUrl"}) {
            try {
                var m = vp.getClass().getMethod(mName);
                Object v = m.invoke(vp);
                if (v != null) {
                    String s = String.valueOf(v).trim();
                    if (!s.isEmpty()) return s;
                }
            } catch (Exception ignore) {
            }
        }
        return null;
    }

    @ModelAttribute("checkout")
    public CheckoutState initCheckout() {
        return new CheckoutState();
    }

    private User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null ? auth.getName() : null);
        if (username == null) throw new IllegalStateException("Chưa đăng nhập");
        return userRepository.findByUsername(username).orElseThrow(() -> new IllegalStateException("Không tìm thấy user"));
    }

    @GetMapping("/details")
    public String details(@RequestParam(value = "vendorServiceId", required = false) Long vendorServiceId,
                          @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
                          @RequestParam(value = "scheduleAt", required = false) String scheduleAtIso,
                          Model model,
                          @ModelAttribute("checkout") CheckoutState state) {

        User user = requireCurrentUser();

        if (vendorServiceId != null) {
            state.source = "buyNow";
            checkoutService.fillBuyNowState(state, user.getId(), vendorServiceId, quantity, scheduleAtIso);
        } else {
            state.source = "cart";
            checkoutService.fillCartState(state, user.getId());
        }

        // Prefill contact từ customer_profile
        try {
            List<?> row = em.createNativeQuery(
                            "select full_name, address_line from customer_profile where user_id = :uid limit 1")
                    .setParameter("uid", user.getId())
                    .getResultList();
            String nameDefault = null, addressDefault = null;
            if (!row.isEmpty()) {
                Object[] r = (Object[]) row.get(0);
                if (r[0] != null) nameDefault = String.valueOf(r[0]);
                if (r[1] != null) addressDefault = String.valueOf(r[1]);
            }
            if ((state.contact.contactName == null || state.contact.contactName.isBlank()) && nameDefault != null) {
                state.contact.contactName = nameDefault;
            }
            if ((state.contact.addressLine == null || state.contact.addressLine.isBlank()) && addressDefault != null) {
                state.contact.addressLine = addressDefault;
            }
        } catch (Exception ignore) {
        }

        state.subtotal = state.items.stream()
                .map(it -> it.subtotal == null ? BigDecimal.ZERO : it.subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        state.discountAmount = BigDecimal.ZERO;
        state.total = state.subtotal;

        model.addAttribute("contactNameDefault", state.contact.contactName);
        model.addAttribute("contactPhoneDefault", state.contact.contactPhone);
        model.addAttribute("addressDefault", state.contact.addressLine);

        model.addAttribute("customer", customerProfileRepository.findByUser_Id(user.getId()).orElse(null));

        model.addAttribute("cartItems", state.items);
        model.addAttribute("subtotal", state.subtotal);
        model.addAttribute("discountAmount", state.discountAmount);
        model.addAttribute("total", state.total);
        model.addAttribute("couponCode", state.couponCode);

        return "customer/checkout/details";
    }

    @PostMapping("/confirm")
    public String postDetailsToConfirm(@RequestParam String contactName,
                                       @RequestParam String contactPhone,
                                       @RequestParam(required = false) String couponCode,
                                       @ModelAttribute("checkout") CheckoutState state,
                                       jakarta.servlet.http.HttpServletRequest request,
                                       @RequestParam(required = false) String addressLine,
                                       @RequestParam(required = false) String provinceId,
                                       @RequestParam(required = false) String districtId,
                                       @RequestParam(required = false) String notes) {

        state.contact.contactName = contactName;
        state.contact.contactPhone = contactPhone;
        state.contact.addressLine = addressLine;
        state.contact.provinceId = provinceId;
        state.contact.districtId = districtId;
        state.contact.notes = notes;
        state.couponCode = (couponCode == null || couponCode.isBlank()) ? null : couponCode.trim();

        for (int i = 0; i < state.items.size(); i++) {
            Item it = state.items.get(i);
            String sched = request.getParameter("items[" + i + "].scheduleAt");
            String note = request.getParameter("items[" + i + "].notes");
            String addr = request.getParameter("items[" + i + "].addressLine");

            if (sched != null && !sched.isBlank()) {
                try {
                    it.scheduledAt = java.time.LocalDateTime.parse(sched);
                } catch (Exception ignore) {
                }
            }
            if (note != null) it.notes = note;
            if (addr != null && !addr.isBlank()) it.addressLine = addr;
            if ((it.addressLine == null || it.addressLine.isBlank()) && state.contact.addressLine != null) {
                it.addressLine = state.contact.addressLine;
            }
        }
        return "redirect:/customer/checkout/confirm";
    }

    @GetMapping("/confirm")
    public String confirm(Model model, @ModelAttribute("checkout") CheckoutState state) {
        User user = requireCurrentUser();

        Map<Long, VendorOrderView> byVendor = new LinkedHashMap<>();
        for (Item it : state.items) {
            VendorOrderView v = byVendor.computeIfAbsent(it.vendorId, k -> {
                VendorOrderView x = new VendorOrderView();
                x.vendor = it.vendor;

                x.vendorAvatarUrl = null;
                try {
                    @SuppressWarnings("unchecked")
                    java.util.List<Object> rs = em.createNativeQuery(
                                    "select avatar_url from `user` where id = :vid limit 1")
                            .setParameter("vid", it.vendorId)
                            .getResultList();
                    if (!rs.isEmpty() && rs.get(0) != null) {
                        String s = String.valueOf(rs.get(0)).trim();
                        if (!s.isEmpty()) x.vendorAvatarUrl = s;
                    }
                } catch (Exception ignored) {
                }

                return x;
            });

            v.items.add(it);
            v.subtotal = v.subtotal.add(it.subtotal == null ? java.math.BigDecimal.ZERO : it.subtotal);
        }

        state.subtotal = byVendor.values().stream()
                .map(v -> v.subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        CouponCalcService.CalcResult calc = couponCalcService.calculateForWholeCheckout(
                user.getId(), state.couponCode, state.items);

        state.discountAmount = calc.totalDiscount();
        state.discountAllocByVendor = calc.discountByVendor();
        for (Map.Entry<Long, VendorOrderView> e : byVendor.entrySet()) {
            BigDecimal alloc = calc.discountByVendor().getOrDefault(e.getKey(), BigDecimal.ZERO);
            e.getValue().discount = alloc;
            e.getValue().total = e.getValue().subtotal.subtract(alloc);
        }
        state.total = state.subtotal.subtract(state.discountAmount);

        model.addAttribute("contactName", state.contact.contactName);
        model.addAttribute("contactPhone", state.contact.contactPhone);
        model.addAttribute("addressLine", state.contact.addressLine);
        model.addAttribute("ordersByVendor", new ArrayList<>(byVendor.values()));
        model.addAttribute("subtotal", state.subtotal);
        model.addAttribute("discountAmount", state.discountAmount);
        model.addAttribute("total", state.total);
        model.addAttribute("couponCode", state.couponCode);

        return "customer/checkout/confirm";
    }

    @PostMapping("/process")
    @Transactional
    public String process(@RequestParam("paymentMethod") String paymentMethod,
                          @ModelAttribute("checkout") CheckoutState state,
                          SessionStatus sessionStatus, Model model) {
        User user = requireCurrentUser();

        String code = (state != null && state.couponCode != null && !"__NONE__".equals(state.couponCode.trim()))
                ? state.couponCode.trim() : null;
        if (code != null && couponRepository.findByCodeIgnoreCase(code).isEmpty()) code = null;

        OrderService.CreateResult r = orderService.createOrdersFromCheckout(
                user, state.items, state.contact, code, state.discountAllocByVendor);

        // KHÔNG clear giỏ ở đây cho VNPay. Chỉ clear ngay với phương thức offline.
        if (!"VNPAY".equalsIgnoreCase(paymentMethod) && "cart".equalsIgnoreCase(state.source)) {
            checkoutService.clearCartItems(user.getId(), state.items);
            sessionStatus.setComplete();
            return "redirect:/customer/orders";
        }

        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            String csv = r.orders.stream().map(o -> String.valueOf(o.getId()))
                    .collect(java.util.stream.Collectors.joining(","));
            return "redirect:/customer/payment/vnpay/checkout?orderIds=" +
                    java.net.URLEncoder.encode(csv, java.nio.charset.StandardCharsets.UTF_8);
        }

        return "redirect:/customer/orders";
    }

    @PostMapping("/apply-coupon")
    public String applyCoupon(@RequestParam(required = false) String couponCode,
                              @ModelAttribute("checkout") CheckoutState state) {
        state.couponCode = (couponCode == null || couponCode.isBlank()) ? null : couponCode.trim();
        return "redirect:/customer/checkout/confirm";
    }

    @PostMapping("/apply-coupon-ajax")
    @ResponseBody
    public Map<String, Object> applyCouponAjax(@RequestParam(required = false) String couponCode,
                                               @ModelAttribute("checkout") CheckoutState state) {
        Map<String, Object> res = new HashMap<>();
        state.couponCode = (couponCode == null || couponCode.isBlank()) ? null : couponCode.trim();

        java.math.BigDecimal subtotal = state.items.stream()
                .map(it -> it.subtotal == null ? java.math.BigDecimal.ZERO : it.subtotal)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        org.springframework.security.core.Authentication auth =
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;
        Long userId = null;
        if (username != null) {
            userId = userRepository.findByUsername(username).map(Project.HouseService.Entity.User::getId).orElse(null);
        }

        var calc = couponCalcService.calculateForWholeCheckout(userId, state.couponCode, state.items);
        state.discountAmount = calc.totalDiscount();
        state.discountAllocByVendor = calc.discountByVendor();
        state.subtotal = subtotal;
        state.total = subtotal.subtract(state.discountAmount);

        res.put("ok", state.discountAmount.compareTo(java.math.BigDecimal.ZERO) > 0);
        res.put("message", (state.couponCode == null) ? "Đã xoá mã" :
                (state.discountAmount.compareTo(java.math.BigDecimal.ZERO) > 0 ? "Áp mã thành công" : "Mã không hợp lệ hoặc hết hạn"));
        res.put("subtotal", state.subtotal);
        res.put("discount", state.discountAmount);
        res.put("total", state.total);
        return res;
    }

    @GetMapping("/eligible-coupons")
    @ResponseBody
    public List<Map<String, Object>> eligibleCoupons(@ModelAttribute("checkout") CheckoutState state) {
        User user = requireCurrentUser();
        java.util.Set<Long> vendorIds = new java.util.HashSet<>();
        java.util.Set<Long> serviceIds = new java.util.HashSet<>();
        for (Item it : state.items) {
            if (it.vendorId != null) vendorIds.add(it.vendorId);
            if (it.vendorService != null) serviceIds.add(it.vendorService.getId());
        }

        java.util.List<Project.HouseService.Entity.Coupon> coupons =
                couponCalcService.findEligibleCouponsForCart(user.getId(), vendorIds, serviceIds);

        java.util.List<Map<String, Object>> out = new java.util.ArrayList<>();
        for (Project.HouseService.Entity.Coupon c : coupons) {
            var calc = couponCalcService.calculateForWholeCheckout(user.getId(), c.getCode(), state.items);
            if (calc.totalDiscount().compareTo(java.math.BigDecimal.ZERO) > 0) {
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("code", c.getCode());
                m.put("name", c.getName());
                m.put("discount", calc.totalDiscount());
                m.put("selected", c.getCode().equalsIgnoreCase(state.couponCode));
                out.add(m);
            }
        }
        out.sort(java.util.Comparator.comparing((Map<String, Object> m) ->
                (java.math.BigDecimal) m.get("discount")).reversed());
        return out;
    }
}
