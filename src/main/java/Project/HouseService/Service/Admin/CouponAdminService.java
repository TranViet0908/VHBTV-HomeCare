// src/main/java/Project/HouseService/Service/Admin/CouponAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.*;
import Project.HouseService.Repository.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class CouponAdminService {

    private final CouponRepository coupons;
    private final CouponUserRepository couponUsers;
    private final CouponVendorRepository couponVendors; // << thêm
    private final CouponRedemptionRepository redemptions;

    public CouponAdminService(CouponRepository coupons,
                              CouponUserRepository couponUsers,
                              CouponVendorRepository couponVendors,
                              CouponRedemptionRepository redemptions) {
        this.coupons = coupons;
        this.couponUsers = couponUsers;
        this.couponVendors = couponVendors;
        this.redemptions = redemptions;
    }

    @Transactional(readOnly = true)
    public Page<Coupon> list(String q, String type, Boolean active,
                             LocalDateTime from, LocalDateTime to,
                             int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        if (q != null && !q.isBlank()) return coupons.findByCodeContainingIgnoreCase(q.trim(), pageable);
        if (type != null && !type.isBlank()) {
            try { return coupons.findByType(Coupon.Type.valueOf(type.trim().toUpperCase()), pageable); }
            catch (IllegalArgumentException ignore) {}
        }
        if (active != null) return coupons.findByActive(active, pageable);
        if (from != null && to != null) return coupons.findByStartAtBeforeAndEndAtAfter(to, from, pageable);
        return coupons.findAll(pageable);
    }

    public Coupon create(Coupon c) {
        if (c.getScope() == null) c.setScope(Coupon.Scope.SERVICE);
        if (c.getName() == null || c.getName().isBlank()) c.setName(c.getCode());
        return coupons.save(c);
    }

    public Coupon update(Long id, Coupon form) {
        Coupon c = require(id);
        c.setCode(form.getCode());
        c.setName((form.getName()==null||form.getName().isBlank()) ? form.getCode() : form.getName());
        c.setType(form.getType());
        c.setScope(form.getScope()==null ? Coupon.Scope.SERVICE : form.getScope());
        c.setValue(form.getValue());
        c.setMaxDiscountAmount(form.getMaxDiscountAmount());
        c.setUsageLimitGlobal(form.getUsageLimitGlobal());
        c.setUsageLimitPerUser(form.getUsageLimitPerUser());
        c.setStartAt(form.getStartAt());
        c.setEndAt(form.getEndAt());
        c.setActive(form.isActive());
        return c;
    }

    public void toggle(Long id) { Coupon c = require(id); c.setActive(!c.isActive()); }

    @Transactional(readOnly = true)
    public Coupon require(Long id) {
        return coupons.findById(id).orElseThrow(() -> new IllegalArgumentException("Coupon not found: " + id));
    }

    @Transactional(readOnly = true)
    public Optional<Coupon> byCode(String code) { return coupons.findByCodeIgnoreCase(code); }

    // Preview: whitelist theo customer và vendor
    @Transactional(readOnly = true)
    public PreviewResult preview(String code, BigDecimal subtotal, Long userId, Long vendorId) {
        if (subtotal == null || subtotal.signum() <= 0) return PreviewResult.invalid("Subtotal không hợp lệ");
        Coupon c = coupons.findByCodeIgnoreCase(code == null ? null : code.trim()).orElse(null);
        if (c == null) return PreviewResult.invalid("Không tìm thấy mã");
        LocalDateTime now = LocalDateTime.now();
        if (!c.isActive()) return PreviewResult.invalid("Mã đang tắt");
        if (c.getStartAt()!=null && now.isBefore(c.getStartAt())) return PreviewResult.invalid("Chưa đến thời gian áp dụng");
        if (c.getEndAt()!=null && now.isAfter(c.getEndAt())) return PreviewResult.invalid("Mã đã hết hạn");

        boolean hasUserWL = couponUsers.countByCoupon_Id(c.getId()) > 0;
        boolean hasVendorWL = couponVendors.countByCoupon_Id(c.getId()) > 0;

        if (hasUserWL && (userId==null || !couponUsers.existsByCoupon_IdAndUser_Id(c.getId(), userId)))
            return PreviewResult.invalid("Bạn không nằm trong danh sách áp mã");
        if (hasVendorWL && (vendorId==null || !couponVendors.existsByCoupon_IdAndVendor_Id(c.getId(), vendorId)))
            return PreviewResult.invalid("Vendor không được áp mã");

        long usedGlobal = redemptions.countByCoupon_Id(c.getId());
        if (c.getUsageLimitGlobal()!=null && c.getUsageLimitGlobal()>0 && usedGlobal >= c.getUsageLimitGlobal())
            return PreviewResult.invalid("Mã đã hết lượt toàn hệ thống");
        if (userId!=null && c.getUsageLimitPerUser()!=null && c.getUsageLimitPerUser()>0) {
            long usedByUser = redemptions.countByCoupon_IdAndUser_Id(c.getId(), userId);
            if (usedByUser >= c.getUsageLimitPerUser()) return PreviewResult.invalid("Bạn đã dùng hết lượt");
        }

        BigDecimal discount;
        if (c.getType()==Coupon.Type.FIXED) {
            discount = subtotal.min(zeroIfNull(c.getValue()));
        } else {
            discount = subtotal.multiply(zeroIfNull(c.getValue()).movePointLeft(2));
            if (c.getMaxDiscountAmount()!=null && c.getMaxDiscountAmount().signum()>0)
                discount = discount.min(c.getMaxDiscountAmount());
        }
        if (discount.signum() <= 0) return PreviewResult.invalid("Mã không tạo ra giảm giá");
        return PreviewResult.valid(discount);
    }

    private BigDecimal zeroIfNull(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }

    public CouponRedemption redeem(Coupon coupon, User user, VendorService vs, BigDecimal amount) {
        CouponRedemption r = new CouponRedemption();
        r.setCoupon(coupon); r.setUser(user); r.setVendorService(vs); r.setAmountDiscounted(amount);
        return redemptions.save(r);
    }

    public static class PreviewResult {
        private final boolean valid; private final String reason; private final BigDecimal discount;
        private PreviewResult(boolean valid,String reason,BigDecimal discount){ this.valid=valid; this.reason=reason; this.discount=discount; }
        public static PreviewResult valid(BigDecimal d){ return new PreviewResult(true,null,d); }
        public static PreviewResult invalid(String reason){ return new PreviewResult(false,reason,null); }
        public boolean isValid(){ return valid; } public String getReason(){ return reason; } public BigDecimal getDiscount(){ return discount; }
    }
}
