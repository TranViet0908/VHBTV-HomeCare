package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Service.Customer.CustomerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/customer")
public class CustomerOrderController {

    private final CustomerOrderService customerOrderService;

    @Autowired(required = false)
    private UserRepository userRepository;
    public CustomerOrderController(CustomerOrderService customerOrderService) {
        this.customerOrderService = customerOrderService;
    }

    @GetMapping("/orders")
    public String redirectOrders() {
        return "redirect:/customer/orders/history";
    }

    // ===== LIST: /customer/orders/history =====
    @GetMapping("/orders/history")
    public String orderHistory(@AuthenticationPrincipal User user,
                               @RequestParam(value = "status", defaultValue = "ALL") String status,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {

        Long userId = requireUserId(user);

        PageRequest pr = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1),50),
                Sort.by(Sort.Direction.DESC,"createdAt"));
        Set<String> statuses = "ALL".equalsIgnoreCase(status)
                ? java.util.Collections.<String>emptySet()
                : java.util.Collections.singleton(status);

        Page<ServiceOrder> pageOrders =
                customerOrderService.listMyOrdersFiltered(userId, statuses, null, null, null, pr);

        List<Map<String,Object>> viewOrders = pageOrders.getContent().stream()
                .map(this::toOrderView).collect(java.util.stream.Collectors.toList());

        Page<ServiceOrder> all = customerOrderService.listMyOrders(userId, Pageable.unpaged());
        java.util.List<ServiceOrder> allList = all.getContent();
        Map<String, Long> counts = new java.util.HashMap<>();
        counts.put("all", all.getTotalElements());
        counts.put("pending", allList.stream().filter(o -> "PENDING".equals(safe(o.getStatus()))).count());
        counts.put("confirmed", allList.stream().filter(o -> "CONFIRMED".equals(safe(o.getStatus()))).count());
        counts.put("inProgress", allList.stream().filter(o -> "IN_PROGRESS".equals(safe(o.getStatus()))).count());
        counts.put("completed", allList.stream().filter(o -> "COMPLETED".equals(safe(o.getStatus()))).count());
        counts.put("cancelled", allList.stream().filter(o -> "CANCELLED".equals(safe(o.getStatus()))).count());

        model.addAttribute("orders", viewOrders);
        model.addAttribute("currentStatus", status);
        model.addAttribute("counts", counts);
        model.addAttribute("currentPage", pageOrders.getNumber());
        model.addAttribute("totalPages", Math.max(pageOrders.getTotalPages(), 1));

        return "customer/orders/list";
    }

    // ===== DETAIL: /customer/orders/detail/{id} =====
    @GetMapping("/orders/detail/{id}")
    public String orderDetailById(@AuthenticationPrincipal User user,
                                  @PathVariable("id") Long orderId,
                                  Model model) {
        Long userId = requireUserId(user);
        ServiceOrder order = customerOrderService.getMyOrderByIdOrThrow(userId, orderId);
        Map<String,Object> view = toOrderDetailView(order);
        model.addAttribute("order", view);
        return "customer/orders/detail";
    }

    // ===== DETAIL BY CODE (nếu cần tái sử dụng) =====
    @GetMapping("/orders/{orderCode}")
    public String orderDetailByCode(@AuthenticationPrincipal User user,
                                    @PathVariable("orderCode") String orderCode,
                                    Model model) {
        Long userId = requireUserId(user);
        ServiceOrder order = customerOrderService.getMyOrderByCodeOrThrow(userId, orderCode);
        Map<String,Object> view = toOrderDetailView(order);
        model.addAttribute("order", view);
        return "customer/orders/detail";
    }

    // ===== ACTIONS =====

    @PostMapping("/orders/{orderCode}/cancel")
    public String cancelOrder(@AuthenticationPrincipal User user,
                              @PathVariable String orderCode,
                              @RequestParam(value = "reason", required = false) String reason,
                              Model model) {
        Long userId = requireUserId(user);
        boolean ok = customerOrderService.cancelMyOrder(userId, orderCode, reason);
        model.addAttribute("cancelOk", ok);
        return "redirect:/customer/orders/" + orderCode;
    }

    @PostMapping("/orders/{orderCode}/items/{itemId}/reschedule")
    public String rescheduleItem(@AuthenticationPrincipal User user,
                                 @PathVariable String orderCode,
                                 @PathVariable Long itemId,
                                 @RequestParam("newTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newTime,
                                 Model model) {
        Long userId = requireUserId(user);
        boolean ok = customerOrderService.rescheduleItem(userId, orderCode, itemId, newTime);
        model.addAttribute("rescheduleOk", ok);
        return "redirect:/customer/orders/" + orderCode;
    }

    // ===== Helpers to build view Map for list/detail (giữ nguyên UI sẵn có) =====
    private java.util.Map<String,Object> toOrderView(ServiceOrder o) {
        var m = new java.util.LinkedHashMap<String,Object>();
        m.put("id", o.getId());
        m.put("orderCode", o.getOrderCode());
        m.put("status", safe(o.getStatus()));
        m.put("statusDisplay", statusDisplay(safe(o.getStatus())));
        m.put("createdAt", o.getCreatedAt());
        m.put("subtotal", getBig(o,"subtotal"));
        m.put("discountAmount", getBig(o,"discountAmount"));
        m.put("total", getBig(o,"total"));

        Payment latest = customerOrderService.getLatestPayment(o.getId()).orElse(null);
        m.put("paymentMethod", latest != null ? safeObj(getField(latest,"provider")) : "—");
        m.put("paymentStatus", latest != null ? safeObj(getField(latest,"status")) : "PENDING");
        m.put("paymentStatusDisplay", paymentStatusDisplay(latest));

        var items = customerOrderService.getItems(o.getId());
        var viewItems = new java.util.ArrayList<java.util.Map<String,Object>>();
        var serviceIds = items.stream().map(this::extractVendorServiceId)
                .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
        var svcById = customerOrderService.loadVendorServicesByIds(serviceIds);

        for (ServiceOrderItem it : items) {
            var iv = new java.util.LinkedHashMap<String,Object>();
            iv.put("quantity", getNum(it,"quantity"));
            iv.put("unitPrice", getBig(it,"unitPrice"));
            iv.put("subtotal", getBig(it,"subtotal"));
            iv.put("scheduleAt", firstNonNull(getDateTime(it,"scheduledAt"), getDateTime(it,"scheduleAt")));
            iv.put("address", firstNonBlank(getStr(it,"address"), getStr(it,"addressLine")));
            iv.put("notes", firstNonBlank(getStr(it,"notes"), getStr(it,"note")));
            Long sid = extractVendorServiceId(it);
            iv.put("vendorService", svcById.get(sid));
            viewItems.add(iv);
        }
        m.put("items", viewItems);

        Long vendorUserId = items.isEmpty() ? null : extractVendorUserIdFromItem(items.get(0));
        var v = new java.util.LinkedHashMap<String,Object>();
        if (vendorUserId != null) {
            // thống kê sao + đơn
            var stats = customerOrderService.getVendorStats(vendorUserId);
            v.put("ratingAvg", stats.get("ratingAvg"));
            v.put("ratingCount", stats.get("ratingCount"));   // số review
            v.put("orderCount", stats.get("orderCount"));     // số đơn COMPLETED

            var vp = customerOrderService.loadVendorsByUserIds(java.util.Set.of(vendorUserId)).get(vendorUserId);
            if (vp != null) {
                v.put("id", vp.getId());
                v.put("slug", makeSlug(vp.getDisplayName()));
                v.put("displayName", vp.getDisplayName());
                v.put("verified", getBool(vp,"verified"));
                try {
                    Object u = getField(vp, "user");
                    Object av = (u != null) ? getField(u, "avatarUrl") : null;
                    if (av != null) v.put("avatarUrl", String.valueOf(av));
                } catch (Exception ignored) {}
            }
        }
        v.putIfAbsent("id", null);
        v.putIfAbsent("slug", null);
        v.putIfAbsent("displayName", "Vendor");
        v.putIfAbsent("verified", Boolean.FALSE);
        v.putIfAbsent("avatarUrl", null);
        v.putIfAbsent("ratingAvg", java.math.BigDecimal.ZERO);
        v.putIfAbsent("ratingCount", 0L);
        v.putIfAbsent("orderCount", 0L);

        m.put("vendor", v);
        return m;
    }

    private static String makeSlug(String s){
        if (s == null) return null;
        String t = s.trim().toLowerCase().replaceAll("\\s+","-");
        t = t.replaceAll("[^a-z0-9\\-]","");
        return t.isEmpty()?null:t;
    }
    private Map<String,Object> toOrderDetailView(ServiceOrder o) {
        Map<String,Object> m = toOrderView(o);
        m.put("contactName", getStr(o,"contactName"));
        m.put("contactPhone", getStr(o,"contactPhone"));
        m.put("addressLine", getStr(o,"addressLine"));
        m.put("notes", getStr(o,"notes"));

        Object couponId = getField(o,"couponId");
        if (couponId instanceof Number) {
            Coupon c = customerOrderService.getCoupon(((Number) couponId).longValue()).orElse(null);
            if (c != null) m.put("coupon", c);
        }
        return m;
    }

    // ===== Auth helpers =====
    private Long requireUserId(User userEntity) {
        if (userEntity != null && userEntity.getId() != null) return userEntity.getId();

        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !a.isAuthenticated()) throw new AccessDeniedException("Unauthorized");

        Object principal = a.getPrincipal();
        Long id = extractIdFromPrincipal(principal);
        if (id != null) return id;

        String username = a.getName();
        if (userRepository != null && username != null) {
            Long fromRepo = findUserIdByUsername(username);
            if (fromRepo != null) return fromRepo;
        }
        throw new AccessDeniedException("Unauthorized");
    }

    private Long extractIdFromPrincipal(Object p) {
        Object id = getField(p, "id");
        if (id instanceof Number) return ((Number) id).longValue();
        Object userObj = getField(p, "user");
        Object uid = userObj == null ? null : getField(userObj, "id");
        if (uid instanceof Number) return ((Number) uid).longValue();
        return null;
    }

    private Long findUserIdByUsername(String username) {
        try {
            Object u = null;
            try {
                java.lang.reflect.Method m = userRepository.getClass().getMethod("findByUsername", String.class);
                u = m.invoke(userRepository, username);
            } catch (NoSuchMethodException e1) {
                try {
                    java.lang.reflect.Method m = userRepository.getClass().getMethod("findFirstByUsername", String.class);
                    u = m.invoke(userRepository, username);
                } catch (NoSuchMethodException e2) {
                    try {
                        java.lang.reflect.Method m = userRepository.getClass().getMethod("findByUser_Username", String.class);
                        u = m.invoke(userRepository, username);
                    } catch (NoSuchMethodException e3) {
                        return null;
                    }
                }
            }
            // unwrap Optional nếu cần
            if (u instanceof java.util.Optional) {
                u = ((java.util.Optional<?>) u).orElse(null);
            }
            Object id = (u == null) ? null : getField(u, "id");
            return (id instanceof Number) ? ((Number) id).longValue() : null;
        } catch (Exception ex) {
            return null;
        }
    }

    // ===== UTIL =====

    private void requireLoggedIn(User user) {
        if (user == null || user.getId() == null) {
            throw new org.springframework.security.access.AccessDeniedException("Unauthorized");
        }
    }

    private static String formatCurrency(BigDecimal v) {
        if (v == null) return "0đ";
        NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
        return nf.format(v) + "đ";
    }

    private static BigDecimal parseVnpAmount(String vnpAmount) {
        try {
            long raw = Long.parseLong(vnpAmount);
            return BigDecimal.valueOf(raw).movePointLeft(2);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static String safe(String s){ return s == null ? "" : s; }
    private static String safeObj(Object o){ return o == null ? "" : o.toString(); }

    private static java.math.BigDecimal getBig(Object bean, String field){
        Object v = getField(bean, field);
        if (v instanceof java.math.BigDecimal) return (java.math.BigDecimal) v;
        if (v instanceof Number) return java.math.BigDecimal.valueOf(((Number)v).doubleValue());
        return java.math.BigDecimal.ZERO;
    }
    private static Integer getNum(Object bean, String field){
        Object v = getField(bean, field);
        if (v instanceof Number) return ((Number)v).intValue();
        return 0;
    }
    private static Boolean getBool(Object bean, String field){
        Object v = getField(bean, field);
        return (v instanceof Boolean) ? (Boolean) v : Boolean.FALSE;
    }
    private static String getStr(Object bean, String field){
        Object v = getField(bean, field);
        return v == null ? null : String.valueOf(v);
    }
    private static java.time.LocalDateTime getDateTime(Object bean, String field){
        Object v = getField(bean, field);
        return (v instanceof java.time.LocalDateTime) ? (java.time.LocalDateTime)v : null;
    }
    private static Object getField(Object bean, String name){
        if (bean == null) return null;
        try {
            java.lang.reflect.Method m = bean.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return m.invoke(bean);
        } catch (Exception e1) {
            try {
                java.lang.reflect.Field f = bean.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return f.get(bean);
            } catch (Exception e2) {
                return null;
            }
        }
    }
    private static <T> T firstNonNull(T a, T b){ return a != null ? a : b; }
    private static String firstNonBlank(String a, String b){ return (a != null && !a.isBlank()) ? a : b; }

    private String statusDisplay(String st){
        return switch (st) {
            case "PENDING" -> "Chờ xác nhận";
            case "CONFIRMED" -> "Đã xác nhận";
            case "IN_PROGRESS" -> "Đang thực hiện";
            case "COMPLETED" -> "Hoàn thành";
            case "CANCELLED" -> "Đã hủy";
            default -> st;
        };
    }
    private String paymentStatusDisplay(Payment p){
        if (p == null) return "Chưa thanh toán";
        String s = safeObj(getField(p,"status"));
        return switch (s) {
            case "PAID" -> "Đã thanh toán";
            case "FAILED" -> "Thanh toán thất bại";
            case "PENDING" -> "Đang chờ thanh toán";
            default -> s;
        };
    }

    private Long extractVendorServiceId(ServiceOrderItem it) {
        Object v = getField(it,"vendorServiceId");
        if (v instanceof Number) return ((Number)v).longValue();
        Object vs = getField(it,"vendorService");
        Object id = vs == null ? null : getField(vs,"id");
        if (id instanceof Number) return ((Number)id).longValue();
        v = getField(it,"serviceId");
        if (v instanceof Number) return ((Number)v).longValue();
        return null;
    }
    private Long extractVendorUserIdFromItem(ServiceOrderItem it) {
        Object v = getField(it,"vendorId");
        if (v instanceof Number) return ((Number)v).longValue();
        Object vendor = getField(it,"vendor");
        Object user = vendor == null ? null : getField(vendor,"user");
        Object id = (user != null) ? getField(user,"id") : getField(vendor,"id");
        if (id instanceof Number) return ((Number)id).longValue();
        return null;
    }
}
