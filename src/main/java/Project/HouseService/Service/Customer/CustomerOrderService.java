package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerOrderService {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CouponRepository couponRepository;
    private final VendorServiceRepository vendorServiceRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final VendorReviewRepository vendorReviewRepository;

    public CustomerOrderService(ServiceOrderRepository serviceOrderRepository,
                                ServiceOrderItemRepository serviceOrderItemRepository,
                                PaymentRepository paymentRepository,
                                CouponRepository couponRepository,
                                VendorServiceRepository vendorServiceRepository,
                                VendorProfileRepository vendorProfileRepository,
                                VendorReviewRepository vendorReviewRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
        this.paymentRepository = paymentRepository;
        this.couponRepository = couponRepository;
        this.vendorServiceRepository = vendorServiceRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.vendorReviewRepository = vendorReviewRepository;
    }

    // ===== LISTING =====

    public Page<ServiceOrder> listMyOrders(Long customerId, Pageable pageable) {
        return serviceOrderRepository.findAllByCustomerId(customerId, pageable);
    }

    public Page<ServiceOrder> listMyOrdersFiltered(Long customerId,
                                                   Set<String> statuses,
                                                   LocalDate from,
                                                   LocalDate to,
                                                   String q,
                                                   Pageable pageable) {
        Page<ServiceOrder> page = serviceOrderRepository.findAllByCustomerId(customerId, pageable);

        List<ServiceOrder> filtered = page.getContent().stream().filter(o -> {
            boolean ok = true;
            if (StringUtils.hasText(q)) {
                String code = nvl(o.getOrderCode());
                ok &= code.toLowerCase().contains(q.toLowerCase());
            }
            if (from != null && o.getCreatedAt() != null) {
                ok &= !o.getCreatedAt().toLocalDate().isBefore(from);
            }
            if (to != null && o.getCreatedAt() != null) {
                ok &= !o.getCreatedAt().toLocalDate().isAfter(to);
            }
            if (statuses != null && !statuses.isEmpty()) {
                ok &= statuses.contains(nvl(o.getStatus()));
            }
            return ok;
        }).collect(Collectors.toList());

        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    // ===== DETAIL =====

    public ServiceOrder getMyOrderByCodeOrThrow(Long customerId, String orderCode) {
        return serviceOrderRepository.findByOrderCodeAndCustomerId(orderCode, customerId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    public ServiceOrder getMyOrderByIdOrThrow(Long customerId, Long orderId) {
        return serviceOrderRepository.findByIdAndCustomerId(orderId, customerId)
                .orElseThrow(() -> new NoSuchElementException("Order not found"));
    }

    public List<ServiceOrderItem> getItems(Long orderId) {
        return serviceOrderItemRepository.findByServiceOrderId(orderId);
    }

    public List<Payment> getPayments(Long orderId) {
        return paymentRepository.findByPayTargetTypeAndPayTargetIdOrderByPaidAtDesc(
                Payment.PayTargetType.SERVICE_ORDER, orderId);
    }
    public Optional<Payment> getLatestPayment(Long orderId) {
        return paymentRepository.findFirstByPayTargetTypeAndPayTargetIdOrderByPaidAtDesc(
                Payment.PayTargetType.SERVICE_ORDER, orderId);
    }

    public Optional<Coupon> getCoupon(Long couponId) {
        if (couponId == null) return Optional.empty();
        return couponRepository.findById(couponId);
    }

    public Map<Long, VendorService> loadVendorServicesByIds(Collection<Long> ids) {
        Map<Long, VendorService> m = new HashMap<>();
        if (ids == null || ids.isEmpty()) return m;
        for (Long id : ids) {
            vendorServiceRepository.findById(id).ifPresent(vs -> m.put(vs.getId(), vs));
        }
        return m;
    }

    public Map<Long, VendorProfile> loadVendorsByUserIds(Collection<Long> userIds) {
        Map<Long, VendorProfile> m = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) return m;

        for (Long uid : userIds) {
            VendorProfile vp = null;

            try {
                Optional<VendorProfile> opt = vendorProfileRepository.findByUser_Id(uid);
                if (opt != null && opt.isPresent()) vp = opt.get();
            } catch (Exception ignored) {}

            if (vp == null) {
                try {
                    vp = vendorProfileRepository.findByUserId(uid);
                } catch (Exception ignored) {}
            }

            if (vp == null) {
                try {
                    Optional<VendorProfile> opt2 = vendorProfileRepository.findFirstByUserId(uid);
                    if (opt2 != null && opt2.isPresent()) vp = opt2.get();
                } catch (Exception ignored) {}
            }

            if (vp != null) {
                Long key = extractVendorUserId(vp);
                if (key != null) m.put(key, vp);
            }
        }
        return m;
    }

    // Lấy thống kê sao + số đơn hoàn tất của vendor theo userId
    public java.util.Map<String,Object> getVendorStats(Long vendorUserId) {
        java.math.BigDecimal ratingAvg = vendorReviewRepository.avgRatingByVendorId(vendorUserId);
        long ratingCount = vendorReviewRepository.countByVendorUserId(vendorUserId);

        long completed = serviceOrderRepository
                .countDistinctByVendorIdAndStatusIn(
                        vendorUserId,
                        java.util.Set.of("COMPLETED")
                );

        java.util.Map<String,Object> m = new java.util.HashMap<>();
        m.put("ratingAvg", ratingAvg == null ? java.math.BigDecimal.ZERO : ratingAvg);
        m.put("ratingCount", ratingCount);
        m.put("completedOrders", completed);
        return m;
    }

    // ===== ACTIONS =====

    public boolean cancelMyOrder(Long customerId, String orderCode, String reason) {
        ServiceOrder o = getMyOrderByCodeOrThrow(customerId, orderCode);
        if (!canCancel(o)) return false;
        o.setStatus("CANCELLED");
        setFieldIfExists(o, Arrays.asList("cancelReason", "noteCancel"), reason);
        serviceOrderRepository.save(o);
        return true;
    }

    public boolean rescheduleItem(Long customerId, String orderCode, Long itemId, LocalDateTime newTime) {
        if (newTime == null || newTime.isBefore(LocalDateTime.now())) return false;
        ServiceOrder o = getMyOrderByCodeOrThrow(customerId, orderCode);
        if (!canReschedule(o)) return false;
        Optional<ServiceOrderItem> opt = serviceOrderItemRepository.findByIdAndServiceOrderId(itemId, o.getId());
        if (opt.isEmpty()) return false;
        ServiceOrderItem it = opt.get();
        setFieldIfExists(it, Arrays.asList("scheduledAt", "scheduleAt", "appointmentAt", "bookingAt"), newTime);
        serviceOrderItemRepository.save(it);
        return true;
    }

    // ===== POLICY =====

    public boolean canCancel(ServiceOrder o) {
        if (o == null) return false;
        String st = nvl(o.getStatus());
        return "PENDING".equals(st);
    }

    public boolean canReschedule(ServiceOrder o) {
        if (o == null) return false;
        String st = nvl(o.getStatus());
        return "CONFIRMED".equals(st) || "IN_PROGRESS".equals(st);
    }

    public boolean canRetryPay(ServiceOrder o, Payment latest) {
        if (o == null) return false;
        String st = nvl(o.getStatus());
        if ("PENDING".equals(st)) return true;
        if (latest == null) return false;
        Object sObj = getFieldValue(latest, "status");
        String ps = sObj == null ? "" : sObj.toString();
        return "FAILED".equalsIgnoreCase(ps);
    }

    // ===== UTIL =====

    private static String nvl(String s) { return s == null ? "" : s; }

    private static void setFieldIfExists(Object bean, List<String> names, Object value) {
        for (String n : names) {
            try {
                Field f = bean.getClass().getDeclaredField(n);
                f.setAccessible(true);
                f.set(bean, value);
                return;
            } catch (NoSuchFieldException ignored) {
            } catch (Exception e) {
                return;
            }
        }
    }

    private static Object getFieldValue(Object bean, String name) {
        try {
            Method m = bean.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return m.invoke(bean);
        } catch (Exception e1) {
            try {
                Field f = bean.getClass().getDeclaredField(name);
                f.setAccessible(true);
                return f.get(bean);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private static Long extractVendorUserId(VendorProfile vp) {
        Object user = getFieldValue(vp, "user");
        if (user != null) {
            Object id = getFieldValue(user, "id");
            if (id instanceof Number) return ((Number) id).longValue();
        }
        Object uid = getFieldValue(vp, "userId");
        if (uid instanceof Number) return ((Number) uid).longValue();
        return null;
    }
}
