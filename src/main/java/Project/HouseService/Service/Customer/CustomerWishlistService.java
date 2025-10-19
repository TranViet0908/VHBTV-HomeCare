package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Entity.WishlistService;
import Project.HouseService.Entity.WishlistVendor;
import Project.HouseService.Repository.VendorServiceRepository;
import Project.HouseService.Repository.WishlistServiceRepository;
import Project.HouseService.Repository.WishlistVendorRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class CustomerWishlistService {

    @PersistenceContext
    private EntityManager em;

    private final WishlistServiceRepository wishlistServiceRepo;
    private final WishlistVendorRepository wishlistVendorRepo;
    private final VendorServiceRepository vendorServiceRepo;

    public CustomerWishlistService(WishlistServiceRepository wishlistServiceRepo,
                                   WishlistVendorRepository wishlistVendorRepo,
                                   VendorServiceRepository vendorServiceRepo) {
        this.wishlistServiceRepo = wishlistServiceRepo;
        this.wishlistVendorRepo = wishlistVendorRepo;
        this.vendorServiceRepo = vendorServiceRepo;
    }

    // ===== Helper =====
    @Transactional(readOnly = true)
    public Long requireUserIdByUsername(String username) {
        try {
            return em.createQuery("select u.id from User u where u.username = :un", Long.class)
                    .setParameter("un", username)
                    .getSingleResult();
        } catch (NoResultException ex) {
            throw new IllegalArgumentException("User not found: " + username);
        }
    }

    // ===== Wishlist SERVICE (by vendorServiceId) =====
    @Transactional
    public boolean addService(Long customerId, Long vendorServiceId) {
        if (!vendorServiceRepo.existsById(vendorServiceId)) {
            throw new IllegalArgumentException("VendorService not found: " + vendorServiceId);
        }
        if (wishlistServiceRepo.existsByCustomer_IdAndVendorService_Id(customerId, vendorServiceId)) return false;

        User customerRef = em.getReference(User.class, customerId);
        VendorService vsRef = em.getReference(VendorService.class, vendorServiceId);
        wishlistServiceRepo.save(new WishlistService(customerRef, vsRef));
        return true;
    }

    @Transactional
    public boolean removeService(Long customerId, Long vendorServiceId) {
        if (!wishlistServiceRepo.existsByCustomer_IdAndVendorService_Id(customerId, vendorServiceId)) return false;
        wishlistServiceRepo.deleteByCustomer_IdAndVendorService_Id(customerId, vendorServiceId);
        return true;
    }

    // ===== Wishlist SERVICE (by wishlistId for /api/customer/wishlist/{id}) =====
    @Transactional
    public boolean removeServiceByWishlistId(Long customerId, Long wishlistId) {
        // bảo đảm chỉ xoá bản ghi của chính customer
        Query q = em.createQuery("delete from WishlistService ws where ws.id = :id and ws.customer.id = :cid");
        int n = q.setParameter("id", wishlistId)
                .setParameter("cid", customerId)
                .executeUpdate();
        return n > 0;
    }

    @Transactional(readOnly = true)
    public boolean isServiceWishlisted(Long customerId, Long vendorServiceId) {
        return wishlistServiceRepo.existsByCustomer_IdAndVendorService_Id(customerId, vendorServiceId);
    }

    @Transactional(readOnly = true)
    public long countService(Long customerId) {
        return wishlistServiceRepo.countByCustomer_Id(customerId);
    }

    /**
     * Dữ liệu cho trang service.html (biến model: wishlistServices)
     * Trả List<Map> với cấu trúc:
     * {
     *   id, createdAt,
     *   vendorService: {
     *     id, title, description, coverUrl, durationMinutes, minNoticeHours, basePrice, unit,
     *     vendor: { id, displayName, ratingAvg }
     *   }
     * }
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listServicePageModels(Long customerId, int limit, int offset) {
        String sql = """
        SELECT
          cw.id,
          cw.created_at,
          vs.id,
          vs.title,
          vs.description,
          vs.cover_url,
          vs.duration_minutes,
          vs.min_notice_hours,
          vs.base_price,
          vs.unit,
          u.id AS vendor_id,
          COALESCE(vp.display_name, u.username) AS vendor_name,
          u.avatar_url AS vendor_avatar_url,
          COALESCE(r.rating_avg, 0)  AS svc_rating_avg,
          COALESCE(r.rating_cnt, 0)  AS svc_rating_cnt
        FROM customer_wishlist cw
        JOIN vendor_service vs ON vs.id = cw.vendor_service_id
        JOIN `user` u          ON u.id = vs.vendor_id
        LEFT JOIN vendor_profile vp ON vp.user_id = u.id
        LEFT JOIN (
           SELECT vendor_service_id, AVG(rating) AS rating_avg, COUNT(1) AS rating_cnt
           FROM vendor_service_review
           GROUP BY vendor_service_id
        ) r ON r.vendor_service_id = vs.id
        WHERE cw.customer_id = :cid
        ORDER BY cw.created_at DESC
        LIMIT :limit OFFSET :offset
        """;
        Query q = em.createNativeQuery(sql)
                .setParameter("cid", customerId)
                .setParameter("limit", Math.max(1, limit))
                .setParameter("offset", Math.max(0, offset));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            int i = 0;
            Long wsId           = ((Number) r[i++]).longValue();
            Object createdAt    = r[i++];
            Long vsId           = ((Number) r[i++]).longValue();
            String title        = (String) r[i++];
            String desc         = (String) r[i++];
            String coverUrl     = (String) r[i++];
            Number durationMin  = (Number) r[i++];
            Number minNoticeH   = (Number) r[i++];
            Number basePrice    = (Number) r[i++];
            String unit         = (String) r[i++];
            Long vendorId       = ((Number) r[i++]).longValue();
            String vendorName   = (String) r[i++];
            String vendorAvatar = (String) r[i++];
            Number svcRatingAvg = (Number) r[i++];
            Number svcRatingCnt = (Number) r[i++];

            Map<String, Object> vendor = new LinkedHashMap<>();
            vendor.put("id", vendorId);
            vendor.put("displayName", vendorName);
            vendor.put("avatarUrl", vendorAvatar);

            Map<String, Object> vs = new LinkedHashMap<>();
            vs.put("id", vsId);
            vs.put("title", title);
            vs.put("description", desc);
            vs.put("coverUrl", coverUrl);
            vs.put("durationMinutes", durationMin);
            vs.put("minNoticeHours", minNoticeH);
            vs.put("basePrice", basePrice);
            vs.put("unit", unit);
            vs.put("ratingAvg", svcRatingAvg);
            vs.put("ratingCount", svcRatingCnt);
            vs.put("vendor", vendor);

            Map<String, Object> ws = new LinkedHashMap<>();
            ws.put("id", wsId);
            ws.put("createdAt", createdAt);
            ws.put("vendorService", vs);

            out.add(ws);
        }
        return out;
    }

    // ===== Wishlist VENDOR =====
    @Transactional
    public boolean addVendor(Long customerId, Long vendorId) {
        if (wishlistVendorRepo.existsByCustomer_IdAndVendor_Id(customerId, vendorId)) return false;
        wishlistVendorRepo.save(new WishlistVendor(
                em.getReference(User.class, customerId),
                em.getReference(User.class, vendorId)
        ));
        return true;
    }

    @Transactional
    public boolean removeVendor(Long customerId, Long vendorId) {
        if (!wishlistVendorRepo.existsByCustomer_IdAndVendor_Id(customerId, vendorId)) return false;
        wishlistVendorRepo.deleteByCustomer_IdAndVendor_Id(customerId, vendorId);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isVendorWishlisted(Long customerId, Long vendorId) {
        return wishlistVendorRepo.existsByCustomer_IdAndVendor_Id(customerId, vendorId);
    }

    @Transactional(readOnly = true)
    public long countVendor(Long customerId) {
        return wishlistVendorRepo.countByCustomer_Id(customerId);
    }

    /**
     * Dữ liệu cho trang vendor.html (biến model: favoriteVendors)
     * Trả List<Map> với key: userId, verified, displayName, bio, ratingAvg, ratingCount, yearsExperience, addressLine, servicesCount
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listVendorPageModels(Long customerId, int limit, int offset) {
        String sql = """
            SELECT
              u.id AS user_id,
              COALESCE(vp.display_name, u.username) AS display_name,
              vp.bio,
              COALESCE(vp.rating_avg, 0)  AS rating_avg,
              COALESCE(vp.rating_count,0) AS rating_count,
              COALESCE(vp.years_experience,0) AS years_experience,
              vp.address_line,
              COALESCE(vp.verified, 0) AS verified,
              (SELECT COUNT(1) FROM vendor_service vs2 WHERE vs2.vendor_id = u.id) AS services_count
            FROM customer_wishlist_vendor cwv
            JOIN `user` u ON u.id = cwv.vendor_id
            LEFT JOIN vendor_profile vp ON vp.user_id = u.id
            WHERE cwv.customer_id = :cid
            ORDER BY cwv.created_at DESC
            LIMIT :limit OFFSET :offset
            """;
        Query q = em.createNativeQuery(sql)
                .setParameter("cid", customerId)
                .setParameter("limit", Math.max(1, limit))
                .setParameter("offset", Math.max(0, offset));

        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<Map<String, Object>> out = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            int i = 0;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", ((Number) r[i++]).longValue());
            m.put("displayName", (String) r[i++]);
            m.put("bio", r[i++]);
            m.put("ratingAvg", r[i++]);
            m.put("ratingCount", r[i++]);
            m.put("yearsExperience", r[i++]);
            m.put("addressLine", r[i++]);
            m.put("verified", ((Number) r[i++]).intValue() != 0);
            m.put("servicesCount", r[i++]);
            out.add(m);
        }
        return out;
    }
}
