// src/main/java/Project/HouseService/Service/Customer/CouponCalcService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Controller.Customer.CheckoutController.Item;
import Project.HouseService.Entity.Coupon;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class CouponCalcService {

    public record CalcResult(BigDecimal totalDiscount, Map<Long, BigDecimal> discountByVendor, Coupon coupon) {}

    public CalcResult calculateForWholeCheckout(Long userId, String couponCode, List<Item> items) {
        if (couponCode == null || couponCode.isBlank() || items == null || items.isEmpty()) {
            return new CalcResult(BigDecimal.ZERO, Map.of(), null);
        }

        Coupon coupon = findCouponByCode(couponCode.trim());
        if (coupon == null || !isActiveNow(coupon)) {
            return new CalcResult(BigDecimal.ZERO, Map.of(), null);
        }

        if (exceedUsageLimit(coupon.getId(), userId, coupon.getUsageLimitGlobal(), coupon.getUsageLimitPerUser())) {
            return new CalcResult(BigDecimal.ZERO, Map.of(), coupon);
        }

        Set<Long> eligibleServiceIds = findEligibleServiceIds(coupon.getId());
        Set<Long> eligibleVendorIds  = eligibleServiceIds.isEmpty() ? findEligibleVendorIds(coupon.getId()) : Set.of();

        Map<Long, BigDecimal> eligibleSubtotalByVendor = new LinkedHashMap<>();
        BigDecimal totalEligible = BigDecimal.ZERO;
        for (Item it : items) {
            boolean ok = (eligibleServiceIds.isEmpty() && eligibleVendorIds.isEmpty())
                    || (!eligibleServiceIds.isEmpty() && it.vendorService != null && eligibleServiceIds.contains(it.vendorService.getId()))
                    || (!eligibleVendorIds.isEmpty() && eligibleVendorIds.contains(it.vendorId));
            if (!ok) continue;

            BigDecimal sub = it.subtotal == null ? BigDecimal.ZERO : it.subtotal;
            eligibleSubtotalByVendor.merge(it.vendorId, sub, BigDecimal::add);
            totalEligible = totalEligible.add(sub);
        }
        if (totalEligible.compareTo(BigDecimal.ZERO) <= 0) {
            return new CalcResult(BigDecimal.ZERO, Map.of(), coupon);
        }

        BigDecimal totalDiscount = switch (coupon.getType()) {
            case PERCENT -> totalEligible.multiply(coupon.getValue()).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            case FIXED -> coupon.getValue();
            default -> BigDecimal.ZERO;
        };
        if (coupon.getMaxDiscountAmount() != null && coupon.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            totalDiscount = totalDiscount.min(coupon.getMaxDiscountAmount());
        }
        totalDiscount = totalDiscount.min(totalEligible);
        if (totalDiscount.compareTo(BigDecimal.ZERO) <= 0) {
            return new CalcResult(BigDecimal.ZERO, Map.of(), coupon);
        }

        Map<Long, BigDecimal> alloc = new LinkedHashMap<>();
        BigDecimal allocated = BigDecimal.ZERO;
        Long maxVendor = null;
        BigDecimal maxSub = BigDecimal.valueOf(-1);
        for (Map.Entry<Long, BigDecimal> e : eligibleSubtotalByVendor.entrySet()) {
            BigDecimal sub = e.getValue();
            if (sub.compareTo(maxSub) > 0) { maxSub = sub; maxVendor = e.getKey(); }
            BigDecimal part = totalDiscount.multiply(sub).divide(totalEligible, 0, RoundingMode.DOWN);
            alloc.put(e.getKey(), part);
            allocated = allocated.add(part);
        }
        if (maxVendor != null) {
            BigDecimal diff = totalDiscount.subtract(allocated);
            if (diff.compareTo(BigDecimal.ZERO) != 0) {
                alloc.put(maxVendor, alloc.getOrDefault(maxVendor, BigDecimal.ZERO).add(diff));
            }
        }

        return new CalcResult(totalDiscount, alloc, coupon);
    }

    private Coupon findCouponByCode(String code) {
        try {
            TypedQuery<Coupon> q = em.createQuery(
                    "select c from Coupon c where lower(c.code) = lower(:code)", Coupon.class);
            q.setParameter("code", code);
            q.setMaxResults(1);
            List<Coupon> list = q.getResultList();
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    private Set<Long> findEligibleServiceIds(Long couponId) {
        List<?> rows = em.createNativeQuery("select vendor_service_id from coupon_service where coupon_id=:cid")
                .setParameter("cid", couponId).getResultList();
        Set<Long> ids = new HashSet<>();
        for (Object r : rows) if (r instanceof Number n) ids.add(n.longValue());
        return ids;
    }

    private Set<Long> findEligibleVendorIds(Long couponId) {
        List<?> rows = em.createNativeQuery("select vendor_id from coupon_vendor where coupon_id=:cid")
                .setParameter("cid", couponId).getResultList();
        Set<Long> ids = new HashSet<>();
        for (Object r : rows) if (r instanceof Number n) ids.add(n.longValue());
        return ids;
    }

    private boolean isActiveNow(Coupon c) {
        try {
            if (!c.isActive()) return false;
        } catch (Exception ignore) {}
        LocalDateTime now = LocalDateTime.now();
        if (c.getStartAt() != null && now.isBefore(c.getStartAt())) return false;
        if (c.getEndAt() != null   && now.isAfter(c.getEndAt())) return false;
        return true;
    }

    private boolean exceedUsageLimit(Long couponId, Long userId, Integer limitGlobal, Integer limitPerUser) {
        if (limitGlobal != null && limitGlobal > 0) {
            Number used = (Number) em.createNativeQuery(
                            "select count(*) from coupon_redemption where coupon_id=:cid")
                    .setParameter("cid", couponId).getSingleResult();
            if (used.longValue() >= limitGlobal) return true;
        }
        if (limitPerUser != null && limitPerUser > 0) {
            Number usedBy = (Number) em.createNativeQuery(
                            "select count(*) from coupon_redemption where coupon_id=:cid and user_id=:uid")
                    .setParameter("cid", couponId).setParameter("uid", userId).getSingleResult();
            if (usedBy.longValue() >= limitPerUser) return true;
        }
        return false;
    }
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager em;

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public java.util.List<Project.HouseService.Entity.Coupon> findEligibleCouponsForCart(
            Long userId, java.util.Set<Long> vendorIds, java.util.Set<Long> serviceIds) {

        String joinCv = vendorIds == null || vendorIds.isEmpty()
                ? "" : " left join coupon_vendor  cv on cv.coupon_id=c.id and cv.vendor_id in (:vendorIds) ";
        String condCv = vendorIds == null || vendorIds.isEmpty()
                ? "" : " or cv.coupon_id is not null ";

        String joinCs = serviceIds == null || serviceIds.isEmpty()
                ? "" : " left join coupon_service cs on cs.coupon_id=c.id and cs.vendor_service_id in (:serviceIds) ";
        String condCs = serviceIds == null || serviceIds.isEmpty()
                ? "" : " or cs.coupon_id is not null ";

        String sql = """
        select distinct c.* from coupon c
        left join coupon_user cu on cu.coupon_id=c.id and cu.user_id=:uid
        """ + joinCv + joinCs + """
        where c.is_active=1
          and (c.start_at is null or c.start_at <= now())
          and (c.end_at   is null or c.end_at   >= now())
          and (
                cu.coupon_id is not null
             """ + condCv + condCs + """
             or (
                 not exists (select 1 from coupon_user  cu2 where cu2.coupon_id=c.id)
                 and not exists (select 1 from coupon_vendor  cv2 where cv2.coupon_id=c.id)
                 and not exists (select 1 from coupon_service cs2 where cs2.coupon_id=c.id)
             )
          )
    """;

        var q = em.createNativeQuery(sql, Project.HouseService.Entity.Coupon.class)
                .setParameter("uid", userId);
        if (vendorIds != null && !vendorIds.isEmpty()) q.setParameter("vendorIds", vendorIds);
        if (serviceIds != null && !serviceIds.isEmpty()) q.setParameter("serviceIds", serviceIds);

        @SuppressWarnings("unchecked")
        java.util.List<Project.HouseService.Entity.Coupon> list = q.getResultList();
        return list;
    }
}
