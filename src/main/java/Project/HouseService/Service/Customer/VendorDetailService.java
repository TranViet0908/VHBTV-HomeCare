// src/main/java/Project/HouseService/Service/Customer/VendorDetailService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.*;
import Project.HouseService.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorDetailService {

    private final UserRepository userRepo;
    private final VendorProfileRepository vendorProfileRepo;
    private final VendorServiceRepository vendorServiceRepo;
    private final VendorServiceMediaRepository mediaRepo;
    private final VendorServiceReviewRepository serviceReviewRepo;
    private final VendorReviewRepository vendorReviewRepo;
    private final CouponRepository couponRepo;
    private final VendorReviewRepository vendorReviewRepository;

    @PersistenceContext
    private EntityManager em;

    public VendorDetailService(UserRepository userRepo,
                               VendorProfileRepository vendorProfileRepo,
                               VendorServiceRepository vendorServiceRepo,
                               VendorServiceMediaRepository mediaRepo,
                               VendorServiceReviewRepository serviceReviewRepo,
                               VendorReviewRepository vendorReviewRepo,
                               CouponRepository couponRepo,
                               VendorReviewRepository vendorReviewRepository) {
        this.userRepo = userRepo;
        this.vendorProfileRepo = vendorProfileRepo;
        this.vendorServiceRepo = vendorServiceRepo;
        this.mediaRepo = mediaRepo;
        this.serviceReviewRepo = serviceReviewRepo;
        this.vendorReviewRepo = vendorReviewRepo;
        this.couponRepo = couponRepo;
        this.vendorReviewRepository = vendorReviewRepository;
    }

    // ===== Helper: resolve vendor theo username hoặc id =====
    public User requireVendorByUsername(String username) {
        return userRepo.findByUsernameAndRole(username, User.Role.ROLE_VENDOR)
                .orElseThrow(() -> new NoSuchElementException("Vendor không tồn tại: " + username));
    }

    public User requireVendorById(Long vendorId) {
        return userRepo.findById(vendorId)
                .filter(u -> u.getRole() == User.Role.ROLE_VENDOR)
                .orElseThrow(() -> new NoSuchElementException("Vendor không tồn tại: " + vendorId));
    }

    // ===== Tổng quan vendor để đẩy vào model 'vendor' (Map) =====
    public Map<String, Object> buildVendorSummary(User vendor) {
        Long vendorId = vendor.getId();
        VendorProfile vp = vendorProfileRepo.findByUserId(vendorId);

        long svcCnt = serviceReviewRepo.countByVendorIdVisible(vendorId);
        Double svcAvg = serviceReviewRepo.avgByVendorIdVisible(vendorId);
        long venCnt = vendorReviewRepo.countVisibleByVendorId(vendorId);
        Double venAvg = vendorReviewRepo.avgByVendorIdVisible(vendorId);
        long totalCnt = svcCnt + venCnt;

        double weightedAvg = 0.0;
        if (totalCnt > 0) {
            double s = (svcAvg != null ? svcAvg : 0.0) * svcCnt
                    + (venAvg != null ? venAvg : 0.0) * venCnt;
            weightedAvg = s / totalCnt;
        }

        String vendorCover = findAnyServiceCover(vendorId);

        Map<String, Object> m = new HashMap<>();
        m.put("id", vendorId);
        m.put("username", vendor.getUsername());
        m.put("email", vendor.getEmail());
        m.put("phoneNumber", vendor.getPhone());
        m.put("avatarUrl", vendor.getAvatarUrl());
        m.put("coverUrl", vendorCover);
        m.put("verified", vp != null ? vp.getVerified() : Boolean.FALSE);
        m.put("displayName", vp != null && vp.getDisplayName() != null && !vp.getDisplayName().isBlank()
                ? vp.getDisplayName() : vendor.getUsername());
        m.put("bio", vp != null ? vp.getBio() : null);
        m.put("yearsExperience", vp != null ? vp.getYearsExperience() : 0);

        // ▼ THÊM DÒNG NÀY: cung cấp addressLine cho template
        m.put("addressLine", (vp != null ? vp.getAddressLine() : null));

        m.put("ratingAvg", java.math.BigDecimal.valueOf(weightedAvg).setScale(1, java.math.RoundingMode.HALF_UP));
        m.put("ratingCount", totalCnt);
        return m;
    }

    private String findAnyServiceCover(Long vendorId) {
        // lấy 1 dịch vụ ACTIVE gần nhất có coverUrl
        List<VendorService> top = vendorServiceRepo.findTopByVendorIdActiveOrderByCreatedDesc(vendorId);
        for (VendorService vs : top) {
            if (vs.getCoverUrl() != null && !vs.getCoverUrl().isBlank()) return vs.getCoverUrl();
            // nếu service không có coverUrl, thử lấy media cover đầu tiên
            List<VendorServiceMedia> medias = mediaRepo.findByVendorService_IdOrderBySortOrderAscIdAsc(vs.getId());
            Optional<VendorServiceMedia> cover = medias.stream().filter(VendorServiceMedia::isCover).findFirst();
            if (cover.isPresent()) return cover.get().getUrl();
            if (!medias.isEmpty()) return medias.get(0).getUrl();
        }
        return null;
    }

    // ===== Danh sách dịch vụ của vendor (Page) =====
    public Page<VendorService> pageVendorServices(Long vendorId, Pageable pageable) {
        Page<VendorService> page;
        try {
            page = vendorServiceRepo.findByVendorIdAndStatusOrderByCreatedAtDesc(
                    vendorId, "ACTIVE", pageable);
        } catch (Exception e) {
            // fallback nếu repo cũ chưa có method Page
            List<VendorService> all = vendorServiceRepo.findByVendorIdAndStatus(vendorId, "ACTIVE");
            int from = Math.min((int) pageable.getOffset(), all.size());
            int to = Math.min(from + pageable.getPageSize(), all.size());
            page = new PageImpl<>(all.subList(from, to), pageable, all.size());
        }

        // làm giàu coverUrl nếu null
        List<Long> vsIds = page.getContent().stream().map(VendorService::getId).toList();
        Map<Long, String> firstMediaCover = new HashMap<>();
        if (!vsIds.isEmpty()) {
            for (Long id : vsIds) {
                List<VendorServiceMedia> medias = mediaRepo.findByVendorService_IdOrderBySortOrderAscIdAsc(id);
                Optional<VendorServiceMedia> cover = medias.stream().filter(VendorServiceMedia::isCover).findFirst();
                firstMediaCover.put(id, cover.map(VendorServiceMedia::getUrl).orElseGet(() ->
                        medias.isEmpty() ? null : medias.get(0).getUrl()));
            }
        }
        page.getContent().forEach(vs -> {
            if (vs.getCoverUrl() == null || vs.getCoverUrl().isBlank()) {
                String url = firstMediaCover.get(vs.getId());
                if (url != null) vs.setCoverUrl(url);
            }
        });
        return page;
    }

    // ===== Coupon đang hiệu lực gắn với vendor (giới hạn để hiển thị block nhỏ) =====
    public List<Coupon> listActiveCouponsForVendor(Long vendorId, int limit) {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> items = couponRepo.findByVendor(vendorId, null, Pageable.ofSize(limit)).getContent();
        return items.stream()
                .filter(Coupon::isActive)
                .filter(c -> (c.getStartAt() == null || !now.isBefore(c.getStartAt()))
                        && (c.getEndAt() == null || !now.isAfter(c.getEndAt())))
                .limit(limit)
                .collect(Collectors.toList());
    }
    public User requireVendorByNameOrUsername(String name) {
        var vpOpt = vendorProfileRepo.findFirstByDisplayNameIgnoreCase(name);
        if (vpOpt.isPresent()) {
            var vp = vpOpt.get();
            return requireVendorById(vp.getUser().getId());
        }
        return requireVendorByUsername(name);
    }
    // GỌI HÀM NÀY sau khi bạn đã xác định được vendorUserId (id của user/vendor)
    public Map<String, Object> computeVendorStats(Long vendorUserId) {
        Map<String, Object> out = new HashMap<>();
        if (vendorUserId == null) {
            out.put("vendorRatingCount", 0L);
            out.put("vendorRatingAvg", 0.0);
            out.put("serviceCount", 0L);
            out.put("totalOrders", 0L);
            return out;
        }

        Number cnt = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM vendor_review " +
                                "WHERE vendor_id = :vid AND COALESCE(hidden,0)=0")
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        Number avg = (Number) em.createNativeQuery(
                        "SELECT COALESCE(AVG(rating),0) FROM vendor_review " +
                                "WHERE vendor_id = :vid AND COALESCE(hidden,0)=0")
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        Number svcCnt = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM vendor_service " +
                                "WHERE vendor_id = :vid AND status <> 'HIDDEN'")
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        // FIX: join đúng cột service_order_id và lọc trạng thái đã hoàn tất
        Number orders = (Number) em.createNativeQuery(
                        "SELECT COUNT(DISTINCT o.id) " +
                                "FROM service_order o " +
                                "JOIN service_order_item i ON i.service_order_id = o.id " +
                                "WHERE i.vendor_id = :vid " +
                                "  AND o.status IN ('COMPLETED')")
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        out.put("vendorRatingCount", cnt == null ? 0L : cnt.longValue());
        out.put("vendorRatingAvg",   avg == null ? 0.0 : ((Number) avg).doubleValue());
        out.put("serviceCount",      svcCnt == null ? 0L : svcCnt.longValue());
        out.put("totalOrders",       orders == null ? 0L : orders.longValue());
        return out;
    }
    @jakarta.transaction.Transactional
    public org.springframework.data.domain.Page<Project.HouseService.Entity.VendorService>
    pageVendorServicesIncludingPaused(Long vendorUserId,
                                      org.springframework.data.domain.Pageable pageable) {
        String jpql = """
        SELECT v FROM VendorService v
        WHERE v.vendorId = :vid AND v.status IN ('ACTIVE','PAUSED')
        ORDER BY v.id DESC
        """;
        String countJpql = """
        SELECT COUNT(v) FROM VendorService v
        WHERE v.vendorId = :vid AND v.status IN ('ACTIVE','PAUSED')
        """;

        var listQ = em.createQuery(jpql, Project.HouseService.Entity.VendorService.class)
                .setParameter("vid", vendorUserId)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize());
        var items = listQ.getResultList();

        Long total = em.createQuery(countJpql, Long.class)
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        return new org.springframework.data.domain.PageImpl<>(items, pageable, total);
    }
}