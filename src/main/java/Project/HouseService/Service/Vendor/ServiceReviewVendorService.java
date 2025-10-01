// src/main/java/Project/HouseService/Service/Vendor/ServiceReviewVendorService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.VendorService;
import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional(readOnly = true)
public class ServiceReviewVendorService {

    private final VendorServiceReviewRepository reviewRepo;
    private final VendorServiceRepository vendorServiceRepo;
    private final ServiceRepository serviceRepo;
    private final CustomerProfileRepository customerProfileRepo;
    private final ServiceOrderItemRepository orderItemRepo;
    private final VendorProfileRepository vendorProfileRepo;

    @PersistenceContext
    private EntityManager em;

    public ServiceReviewVendorService(VendorServiceReviewRepository reviewRepo,
                                      VendorServiceRepository vendorServiceRepo,
                                      ServiceRepository serviceRepo,
                                      CustomerProfileRepository customerProfileRepo,
                                      ServiceOrderItemRepository orderItemRepo,
                                      VendorProfileRepository vendorProfileRepo) {
        this.reviewRepo = reviewRepo;
        this.vendorServiceRepo = vendorServiceRepo;
        this.serviceRepo = serviceRepo;
        this.customerProfileRepo = customerProfileRepo;
        this.orderItemRepo = orderItemRepo;
        this.vendorProfileRepo = vendorProfileRepo;
    }

    // ================== Resolver ==================

    /** Lấy vendorUserId để lọc bảng vendor_service_review.vendor_id (đang lưu user.id) */
    public Long resolveVendorUserId(Long vendorProfileIdHeader, Long userIdHeader) {
        // Ưu tiên X-USER-ID
        if (userIdHeader != null) return userIdHeader;

        // Có X-VENDOR-ID (vendor_profile.id) -> suy ra user.id
        if (vendorProfileIdHeader != null) {
            Optional<VendorProfile> vp = vendorProfileRepo.findById(vendorProfileIdHeader);
            if (vp.isPresent() && vp.get().getUser() != null) return vp.get().getUser().getId();
        }

        // Fallback theo SecurityContext
        Authentication auth = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication() : null;
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            String name = auth.getName();
            try {
                VendorProfile v1 = vendorProfileRepo.findByUserUsername(name);
                if (v1 != null && v1.getUser() != null) return v1.getUser().getId();
            } catch (Exception ignore) {}
            try {
                VendorProfile v2 = vendorProfileRepo.findByUserEmail(name);
                if (v2 != null && v2.getUser() != null) return v2.getUser().getId();
            } catch (Exception ignore) {}
        }

        return 0L; // vẫn render rỗng nếu chưa xác định
    }

    /** Lấy vendorProfileId để liệt kê dịch vụ thuộc vendor (vendor_service.vendor_id = vendor_profile.id) */
    public Long resolveVendorProfileId(Long vendorProfileIdHeader, Long userIdHeader) {
        if (vendorProfileIdHeader != null) return vendorProfileIdHeader;
        if (userIdHeader != null) {
            VendorProfile vp = vendorProfileRepo.findByUserId(userIdHeader);
            if (vp != null) return vp.getId();
        }
        Authentication auth = SecurityContextHolder.getContext() != null
                ? SecurityContextHolder.getContext().getAuthentication() : null;
        if (auth != null && auth.isAuthenticated() && auth.getName() != null) {
            String name = auth.getName();
            try { VendorProfile v1 = vendorProfileRepo.findByUserUsername(name); if (v1 != null) return v1.getId(); } catch (Exception ignore) {}
            try { VendorProfile v2 = vendorProfileRepo.findByUserEmail(name);    if (v2 != null) return v2.getId(); } catch (Exception ignore) {}
        }
        return 0L;
    }

    // ================== Query chính: dùng vendorUserId ==================
    public Page<VendorServiceReview> search(Long vendorUserId,
                                            Long serviceId,
                                            Integer ratingMin,
                                            Integer ratingMax,
                                            LocalDate fromDate,
                                            LocalDate toDate,
                                            String keyword,
                                            boolean includeHidden,
                                            int page, int size) {

        StringBuilder jpql = new StringBuilder("FROM VendorServiceReview r WHERE r.vendorId = :vendorUserId ");
        Map<String, Object> params = new HashMap<>();
        params.put("vendorUserId", vendorUserId);

        if (serviceId != null) {
            jpql.append("AND r.vendorServiceId = :serviceId ");
            params.put("serviceId", serviceId);
        }
        if (ratingMin != null) {
            jpql.append("AND r.rating >= :ratingMin ");
            params.put("ratingMin", ratingMin);
        }
        if (ratingMax != null) {
            jpql.append("AND r.rating <= :ratingMax ");
            params.put("ratingMax", ratingMax);
        }
        LocalDateTime fromAt = null, toAt = null;
        if (fromDate != null) {
            fromAt = fromDate.atStartOfDay(ZoneId.of("Asia/Bangkok")).toLocalDateTime();
            jpql.append("AND r.createdAt >= :fromAt ");
            params.put("fromAt", fromAt);
        }
        if (toDate != null) {
            toAt = toDate.plusDays(1).atStartOfDay(ZoneId.of("Asia/Bangkok")).toLocalDateTime();
            jpql.append("AND r.createdAt < :toAt ");
            params.put("toAt", toAt);
        }
        if (keyword != null && !keyword.isBlank()) {
            jpql.append("AND LOWER(r.content) LIKE LOWER(CONCAT('%', :kw, '%')) ");
            params.put("kw", keyword.trim());
        }
        if (!includeHidden) {
            jpql.append("AND r.hidden = FALSE ");
        }

        String order = " ORDER BY r.createdAt DESC";
        String dataJpql = "SELECT r " + jpql + order;
        String countJpql = "SELECT COUNT(r.id) " + jpql;

        TypedQuery<VendorServiceReview> dataQuery = em.createQuery(dataJpql, VendorServiceReview.class);
        TypedQuery<Long> countQuery = em.createQuery(countJpql, Long.class);
        params.forEach((k, v) -> {
            dataQuery.setParameter(k, v);
            countQuery.setParameter(k, v);
        });

        dataQuery.setFirstResult(page * size);
        dataQuery.setMaxResults(size);

        List<VendorServiceReview> content = dataQuery.getResultList();
        long total = countQuery.getSingleResult();
        return new PageImpl<>(content, org.springframework.data.domain.PageRequest.of(page, size), total);
    }

    // ================== Thống kê: dùng vendorUserId ==================
    public Map<String, Object> summary(Long vendorUserId) {
        Map<String, Object> m = new HashMap<>();

        Double avg = em.createQuery(
                "SELECT COALESCE(AVG(r.rating),0) FROM VendorServiceReview r WHERE r.vendorId=:vuid AND r.hidden=FALSE",
                Double.class
        ).setParameter("vuid", vendorUserId).getSingleResult();

        Long total = em.createQuery(
                "SELECT COUNT(r.id) FROM VendorServiceReview r WHERE r.vendorId=:vuid AND r.hidden=FALSE",
                Long.class
        ).setParameter("vuid", vendorUserId).getSingleResult();

        List<Object[]> hist = em.createQuery(
                "SELECT r.rating, COUNT(r) FROM VendorServiceReview r WHERE r.vendorId=:vuid AND r.hidden=FALSE GROUP BY r.rating",
                Object[].class
        ).setParameter("vuid", vendorUserId).getResultList();

        List<Object[]> top = em.createQuery(
                "SELECT r.vendorServiceId, AVG(r.rating), COUNT(r) " +
                        "FROM VendorServiceReview r " +
                        "WHERE r.vendorId=:vuid AND r.hidden=FALSE " +
                        "GROUP BY r.vendorServiceId " +
                        "ORDER BY AVG(r.rating) DESC, COUNT(r) DESC",
                Object[].class
        ).setParameter("vuid", vendorUserId).getResultList();

        m.put("avg", avg);
        m.put("total", total);
        m.put("histogram", hist);
        m.put("topServices", top);
        return m;
    }

    /** Liệt kê dịch vụ theo vendorProfileId */
    public List<VendorService> servicesOfVendor(Long vendorProfileId) {
        return vendorServiceRepo.findByVendorIdOrderByTitleAsc(vendorProfileId);
    }

    public Optional<VendorService> findVendorService(Long vendorServiceId) {
        return vendorServiceRepo.findById(vendorServiceId);
    }

    // ================== Ẩn/hiện: kiểm tra theo vendorUserId ==================
    @Transactional
    public void setHidden(Long vendorUserId, Long reviewId, boolean hidden) {
        VendorServiceReview r = reviewRepo.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review không tồn tại"));
        if (!Objects.equals(r.getVendorId(), vendorUserId)) {
            throw new IllegalArgumentException("Không có quyền với review này");
        }
        r.setHidden(hidden);
        reviewRepo.save(r);
    }
}
