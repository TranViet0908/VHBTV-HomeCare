// src/main/java/Project/HouseService/Service/Customer/VendorBrowsingService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.CustomerEvent;
import Project.HouseService.Entity.Service;
import Project.HouseService.Repository.CustomerEventRepository;
import Project.HouseService.Repository.ServiceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
// ✗ BỎ DÒNG SAI — alias import không hợp lệ trong Java
// import org.springframework.stereotype.Service as Svc;

import java.time.LocalDateTime;
import java.util.*;

@org.springframework.stereotype.Service // ✓ dùng FQN để tránh trùng tên với Entity Service
@Transactional
public class VendorBrowsingService {

    @PersistenceContext
    private EntityManager em;

    private final ServiceRepository serviceRepository;
    private final CustomerEventRepository customerEventRepository;

    public VendorBrowsingService(ServiceRepository serviceRepository,
                                 CustomerEventRepository customerEventRepository) {
        this.serviceRepository = serviceRepository;
        this.customerEventRepository = customerEventRepository;
    }

    public record Filters(
            String q,
            Boolean verified,
            Long priceMin,
            Long priceMax,
            Integer durationMin,
            Integer durationMax,
            Integer noticeMax
    ) {}

    public static class BrowseResult {
        public final Service service;
        public final long totalVendors;
        public final Page<Map<String, Object>> page;

        public BrowseResult(Service service, long totalVendors, Page<Map<String, Object>> page) {
            this.service = service;
            this.totalVendors = totalVendors;
            this.page = page;
        }
    }

    public BrowseResult findVendorServicesByServiceSlug(
            String slug,
            Filters f,
            Pageable pageable,
            String sort,          // "recommend" | "price-asc" | "price-desc" | "rating" | "newest"
            Long currentUserId,   // hiện chưa dùng vì CustomerEvent không có userId
            String sessionId
    ) {
        Service svc = serviceRepository.findBySlug(slug)
                .orElseThrow(() -> new NoSuchElementException("Service not found: " + slug));

        // Log sự kiện mở danh sách
        tryLogViewVendorList(svc.getId(), sessionId, f, sort, pageable);

        // WHERE + params
        StringBuilder where = new StringBuilder("""
            FROM vendor_service vs
            JOIN service s ON s.id = vs.service_id
            JOIN vendor_profile vp ON vp.user_id = vs.vendor_id
            WHERE s.slug = :slug AND vs.status = 'ACTIVE'
            """);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("slug", slug);

        if (f != null) {
            if (f.q() != null && !f.q().isBlank()) {
                where.append("""
                    AND (LOWER(vp.display_name) LIKE LOWER(CONCAT('%', :q, '%'))
                      OR LOWER(vs.title)        LIKE LOWER(CONCAT('%', :q, '%')))
                    """);
                params.put("q", f.q().trim());
            }
            if (f.verified() != null) { where.append(" AND vp.verified = :verified "); params.put("verified", f.verified()); }
            if (f.priceMin() != null) { where.append(" AND vs.base_price >= :priceMin "); params.put("priceMin", f.priceMin()); }
            if (f.priceMax() != null) { where.append(" AND vs.base_price <= :priceMax "); params.put("priceMax", f.priceMax()); }
            if (f.durationMin() != null) { where.append(" AND vs.duration_minutes >= :durationMin "); params.put("durationMin", f.durationMin()); }
            if (f.durationMax() != null) { where.append(" AND vs.duration_minutes <= :durationMax "); params.put("durationMax", f.durationMax()); }
            if (f.noticeMax() != null)   { where.append(" AND vs.min_notice_hours <= :noticeMax "); params.put("noticeMax", f.noticeMax()); }
        }

        // SELECT
        String select = """
            SELECT
              vs.id                           AS id,
              vs.title                        AS title,
              vs.description                  AS vs_description,
              vs.base_price                   AS base_price,
              vs.unit                         AS unit,
              vs.duration_minutes             AS duration_minutes,
              vs.min_notice_hours             AS min_notice_hours,
              COALESCE(
                 vs.cover_url,
                 (SELECT m.url FROM vendor_service_media m
                  WHERE m.vendor_service_id = vs.id
                  ORDER BY m.is_cover DESC, m.sort_order ASC, m.id ASC
                  LIMIT 1)
              )                               AS cover_url,
              vp.user_id                      AS vendor_id,
              vp.display_name                 AS display_name,
              COALESCE(vp.rating_avg,0)       AS rating_avg,
              COALESCE(vp.rating_count,0)     AS rating_count,
              COALESCE(vp.years_experience,0) AS years_experience,
              vp.verified                     AS verified,
              vp.address_line                 AS address_line,
              COALESCE(vs.created_at, vs.id)  AS created_sort
            """;

        // ORDER BY
        String order;
        switch (sort == null ? "recommend" : sort) {
            case "price-asc" -> order = " ORDER BY vs.base_price ASC, vs.id DESC ";
            case "price-desc" -> order = " ORDER BY vs.base_price DESC, vs.id DESC ";
            case "rating" -> order = " ORDER BY rating_avg DESC, rating_count DESC, vs.id DESC ";
            case "newest" -> order = " ORDER BY created_sort DESC, vs.id DESC ";
            case "recommend" -> {
                String score = "(rating_avg * LN(1 + rating_count) + 0.5*CASE WHEN vp.verified THEN 1 ELSE 0 END + 0.1*years_experience)";
                order = " ORDER BY " + score + " DESC, rating_avg DESC, rating_count DESC, vs.id DESC ";
            }
            default -> order = " ORDER BY vs.id DESC ";
        }

        // Main query
        String sql = select + " " + where + " " + order;
        Query listQ = em.createNativeQuery(sql);
        params.forEach(listQ::setParameter);
        listQ.setFirstResult((int) pageable.getOffset());
        listQ.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = listQ.getResultList();
        List<Map<String, Object>> items = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Map<String, Object> vendor = new LinkedHashMap<>();
            vendor.put("vendorId",           toLong(r[8]));
            vendor.put("displayName",        toStr(r[9]));
            vendor.put("ratingAvg",          toDouble(r[10]));
            vendor.put("ratingCount",        toLong(r[11]));
            vendor.put("yearsExperience",    toInt(r[12]));
            vendor.put("verified",           toBool(r[13]));
            vendor.put("addressLine",        toStr(r[14]));

            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id",               toLong(r[0]));
            m.put("title",            toStr(r[1]));
            m.put("description",      toStr(r[2]));
            m.put("basePrice",        toLong(r[3]));
            m.put("unit",             toStr(r[4]));
            m.put("durationMinutes",  toInt(r[5]));
            m.put("minNoticeHours",   toInt(r[6]));
            m.put("coverUrl",         toStr(r[7]));
            m.put("vendor",           vendor);
            items.add(m);
        }

        // Count for pagination
        String countSql = "SELECT COUNT(1) " + where;
        Query cntQ = em.createNativeQuery(countSql);
        params.forEach(cntQ::setParameter);
        long total = ((Number) cntQ.getSingleResult()).longValue();

        // Count distinct vendors for header
        String distinctVSql = "SELECT COUNT(DISTINCT vs.vendor_id) " + where;
        Query dvQ = em.createNativeQuery(distinctVSql);
        params.forEach(dvQ::setParameter);
        long totalVendors = ((Number) dvQ.getSingleResult()).longValue();

        Page<Map<String, Object>> pageObj = new PageImpl<>(items, pageable, total);
        return new BrowseResult(svc, totalVendors, pageObj);
    }

    private void tryLogViewVendorList(Long serviceId,
                                      String sessionId,
                                      Filters f, String sort, Pageable pageable) {
        try {
            CustomerEvent e = new CustomerEvent();
            // ✓ enum thay vì String
            e.setAction(CustomerEvent.Action.VIEW_VENDOR_LIST);
            e.setServiceId(serviceId);
            e.setVendorId(null);
            e.setVendorServiceId(null);
            // ✓ CustomerEvent có sessionId + occurredAt + meta
            try {
                CustomerEvent.class.getMethod("setSessionId", String.class).invoke(e, sessionId);
            } catch (NoSuchMethodException ignore) {}
            try {
                CustomerEvent.class.getMethod("setOccurredAt", LocalDateTime.class).invoke(e, LocalDateTime.now());
            } catch (NoSuchMethodException ignore) {}
            String meta = String.format("q=%s;sort=%s;page=%d;size=%d",
                    f != null ? safe(f.q()) : null,
                    sort,
                    pageable.getPageNumber(),
                    pageable.getPageSize());
            try {
                CustomerEvent.class.getMethod("setMeta", String.class).invoke(e, meta);
            } catch (NoSuchMethodException ignore) {}
            customerEventRepository.save(e);
        } catch (Exception ignore) {
            // không chặn render nếu log lỗi
        }
    }

    private static String safe(String s) { return s == null ? null : s.length() > 200 ? s.substring(0,200) : s; }

    private static Long toLong(Object o){ return o==null?null:((Number)o).longValue(); }
    private static Integer toInt(Object o){ return o==null?null:((Number)o).intValue(); }
    private static Double toDouble(Object o){ return o==null?0.0:((Number)o).doubleValue(); }
    private static String toStr(Object o){ return o==null?null:o.toString(); }
    private static Boolean toBool(Object o){
        if (o==null) return null;
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.intValue()!=0;
        return Boolean.parseBoolean(o.toString());
    }
}
