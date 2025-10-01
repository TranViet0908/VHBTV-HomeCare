// src/main/java/Project/HouseService/Service/Vendor/VendorReviewService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.VendorReview;
import Project.HouseService.Repository.VendorReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class VendorReviewService {

    private final VendorReviewRepository reviewRepo;

    @PersistenceContext
    private EntityManager em;

    public VendorReviewService(VendorReviewRepository reviewRepo) {
        this.reviewRepo = reviewRepo;
    }

    public static class Summary {
        public final double avg;
        public final long count;
        public final List<VendorReview> last5;
        public Summary(double avg, long count, List<VendorReview> last5) {
            this.avg = avg; this.count = count; this.last5 = last5;
        }
    }

    public Summary getVendorSummary(Long vendorUserId) {
        // count
        Long count = em.createQuery("""
        SELECT COUNT(r) FROM VendorReview r
        WHERE r.vendorId = :vid AND r.hidden = false
    """, Long.class)
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        // average
        Double avg = em.createQuery("""
        SELECT AVG(r.rating) FROM VendorReview r
        WHERE r.vendorId = :vid AND r.hidden = false
    """, Double.class)
                .setParameter("vid", vendorUserId)
                .getSingleResult();
        double avgVal = (avg != null) ? avg.doubleValue() : 0.0;

        // last 5
        List<VendorReview> last5 = em.createQuery("""
        SELECT r FROM VendorReview r
        WHERE r.vendorId = :vid AND r.hidden = false
        ORDER BY r.createdAt DESC
    """, VendorReview.class)
                .setParameter("vid", vendorUserId)
                .setMaxResults(5)
                .getResultList();

        return new Summary(avgVal, (count != null ? count : 0L), last5);
    }

    public Map<Integer, Long> getRatingDistribution(Long vendorUserId) {
        List<Object[]> rows = em.createQuery("""
        SELECT r.rating, COUNT(r) FROM VendorReview r
        WHERE r.vendorId = :vid AND r.hidden = false AND r.rating IS NOT NULL
        GROUP BY r.rating
    """, Object[].class).setParameter("vid", vendorUserId).getResultList();

        Map<Integer, Long> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) map.put(i, 0L);
        for (Object[] r : rows) {
            Integer k = ((Number) r[0]).intValue();
            Long v = ((Number) r[1]).longValue();
            map.put(k, v);
        }
        return map;
    }

    public Page<VendorReview> searchReviews(Long vendorId,
                                            Integer rating,
                                            LocalDate start,
                                            LocalDate end,
                                            Boolean hasContent,
                                            String keyword,
                                            String sortField,
                                            String sortDir,
                                            Pageable pageable) {

        StringBuilder where = new StringBuilder(" WHERE r.vendorId = :vid AND r.hidden = false ");
        Map<String, Object> params = new HashMap<>();
        params.put("vid", vendorId);

        if (rating != null && rating >= 1 && rating <= 5) {
            where.append(" AND r.rating = :rating ");
            params.put("rating", rating);
        }
        if (start != null) {
            where.append(" AND r.createdAt >= :start ");
            params.put("start", LocalDateTime.of(start, LocalTime.MIN));
        }
        if (end != null) {
            where.append(" AND r.createdAt < :end ");
            params.put("end", LocalDateTime.of(end.plusDays(1), LocalTime.MIN));
        }
        if (hasContent != null) {
            if (hasContent) where.append(" AND r.content IS NOT NULL AND r.content <> '' ");
            else where.append(" AND (r.content IS NULL OR r.content = '') ");
        }
        if (StringUtils.hasText(keyword)) {
            where.append(" AND LOWER(r.content) LIKE LOWER(CONCAT('%', :kw, '%')) ");
            params.put("kw", keyword.trim());
        }

        String order = " ORDER BY r.createdAt DESC ";
        if ("rating".equalsIgnoreCase(sortField)) {
            order = " ORDER BY r.rating " + ("ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC");
        } else if ("createdAt".equalsIgnoreCase(sortField)) {
            order = " ORDER BY r.createdAt " + ("ASC".equalsIgnoreCase(sortDir) ? "ASC" : "DESC");
        }

        TypedQuery<VendorReview> dataQ = em.createQuery(
                "SELECT r FROM VendorReview r " + where + order, VendorReview.class);
        params.forEach(dataQ::setParameter);
        dataQ.setFirstResult((int) pageable.getOffset());
        dataQ.setMaxResults(pageable.getPageSize());
        var data = dataQ.getResultList();

        TypedQuery<Long> cntQ = em.createQuery(
                "SELECT COUNT(r) FROM VendorReview r " + where, Long.class);
        params.forEach(cntQ::setParameter);
        long total = cntQ.getSingleResult();

        return new PageImpl<>(data, pageable, total);
    }
}
