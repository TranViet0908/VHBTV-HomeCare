package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.CouponUser;
import Project.HouseService.Entity.User;
import Project.HouseService.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CouponVendorService {

    private final CouponRepository couponRepository;
    private final CouponVendorRepository couponVendorRepository;
    private final CouponUserRepository couponUserRepository;
    private final CouponRedemptionRepository couponRedemptionRepository;
    private final UserRepository userRepository;

    public CouponVendorService(CouponRepository couponRepository,
                               CouponVendorRepository couponVendorRepository,
                               CouponUserRepository couponUserRepository,
                               CouponRedemptionRepository couponRedemptionRepository,
                               UserRepository userRepository) {
        this.couponRepository = couponRepository;
        this.couponVendorRepository = couponVendorRepository;
        this.couponUserRepository = couponUserRepository;
        this.couponRedemptionRepository = couponRedemptionRepository;
        this.userRepository = userRepository;
    }

    /** Lấy vendorUserId = user.id của vendor đang đăng nhập */
    public Long currentVendorUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (auth != null) ? auth.getName() : null;
        if (username == null) throw new IllegalStateException("Không xác định được tài khoản đăng nhập");
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user: " + username));
        return u.getId();
    }

    /** Danh sách coupon của vendor theo user_id */
    public Page<Coupon> listCouponsForVendor(Long vendorUserId, String q, Pageable pageable) {
        return couponRepository.findByVendor(vendorUserId, q, pageable);
    }

    public Map<Long, Long> countAssignedUsers(Collection<Long> couponIds) {
        Map<Long, Long> m = new HashMap<>();
        for (Long id : couponIds) m.put(id, couponUserRepository.countByCoupon_Id(id));
        return m;
    }

    public Map<Long, Long> countRedemptions(Collection<Long> couponIds) {
        Map<Long, Long> m = new HashMap<>();
        for (Long id : couponIds) m.put(id, couponRedemptionRepository.countByCoupon_Id(id));
        return m;
    }

    public void assertVendorOwnsCoupon(Long vendorUserId, Long couponId) {
        boolean ok = couponVendorRepository.existsByCoupon_IdAndVendor_Id(couponId, vendorUserId);
        if (!ok) throw new IllegalArgumentException("Vendor không có quyền với coupon #" + couponId);
    }

    public List<CouponUser> listAssignedUsers(Long vendorUserId, Long couponId) {
        assertVendorOwnsCoupon(vendorUserId, couponId);
        return couponUserRepository.findByCoupon_Id(couponId);
    }

    @Transactional
    public int assignUsers(Long vendorUserId, Long couponId, List<Long> userIds) {
        assertVendorOwnsCoupon(vendorUserId, couponId);
        var coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy coupon #" + couponId));
        int created = 0;
        if (userIds == null) return 0;
        for (Long uid : userIds) {
            if (uid == null) continue;
            if (couponUserRepository.existsByCoupon_IdAndUser_Id(couponId, uid)) continue;
            var user = userRepository.findById(uid)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user #" + uid));
            var cu = new Project.HouseService.Entity.CouponUser();
            cu.setCoupon(coupon);
            cu.setUser(user);
            couponUserRepository.save(cu);
            created++;
        }
        return created;
    }

    @Transactional
    public void removeUser(Long vendorUserId, Long couponId, Long userId) {
        assertVendorOwnsCoupon(vendorUserId, couponId);
        couponUserRepository.deleteByCoupon_IdAndUser_Id(couponId, userId);
    }
}
