package Project.HouseService.Service.Admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportAdminService {

    @PersistenceContext
    private EntityManager em;

    public static class PageSlice<T> {
        public final List<T> content;
        public final long total;
        public final int page;
        public final int size;
        public PageSlice(List<T> content, long total, int page, int size) {
            this.content = content; this.total = total; this.page = page; this.size = size;
        }
    }

    /** Danh sách đơn theo bộ lọc + phân trang.
     * row = [id, created_at, status, total_amount, discount_amount, vendor_name, paid_amount, provider]
     * - Bộ lọc ngày áp vào so.created_at
     * - paid_amount chỉ tính PAYMENT status='PAID' (không lọc theo ngày để hiển thị đủ số đã trả của từng đơn)
     */
    public PageSlice<Object[]> orderReport(LocalDate from, LocalDate toExclusive,
                                           Long vendorId, String status, String provider,
                                           int page, int size) {

        String where = " WHERE so.created_at >= :from AND so.created_at < :to ";
        Map<String, Object> params = new HashMap<>();
        params.put("from", ts(from));
        params.put("to", ts(toExclusive));

        if (vendorId != null) { where += " AND so.vendor_id = :vendorId"; params.put("vendorId", vendorId); }
        if (status != null && !status.isBlank()) { where += " AND so.status = :status"; params.put("status", status); }

        boolean filterProvider = provider != null && !provider.isBlank();
        String joinPayForCount = filterProvider
                ? " LEFT JOIN payment p2 ON p2.pay_target_type='SERVICE_ORDER' AND p2.pay_target_id=so.id AND p2.status='PAID' AND p2.provider=:provider "
                : " ";

        String countSql = "SELECT COUNT(*) FROM service_order so " + joinPayForCount + where;
        Query countQ = em.createNativeQuery(countSql);
        params.forEach(countQ::setParameter);
        if (filterProvider) countQ.setParameter("provider", provider);
        long total = ((Number) countQ.getSingleResult()).longValue();

        String dataSql = """
            SELECT
              so.id,
              so.created_at,
              so.status,
              so.total,
              so.discount_amount,
              COALESCE(vp.display_name, CONCAT('User #', so.vendor_id)) AS vendor_name,
              COALESCE(SUM(CASE WHEN p.status='PAID' THEN p.amount ELSE 0 END),0) AS paid_amount,
              MAX(p.provider) AS provider
            FROM service_order so
            LEFT JOIN vendor_profile vp ON vp.user_id = so.vendor_id
            LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id=so.id
            """ + (filterProvider ? " AND p.provider=:provider " : "") + where + """
            GROUP BY so.id, so.created_at, so.status, so.total, so.discount_amount, vendor_name
            ORDER BY so.created_at DESC
        """;
        Query dataQ = em.createNativeQuery(dataSql);
        params.forEach(dataQ::setParameter);
        if (filterProvider) dataQ.setParameter("provider", provider);
        dataQ.setFirstResult(page * size);
        dataQ.setMaxResults(size);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = dataQ.getResultList();
        return new PageSlice<>(rows, total, page, size);
    }

    /** Tổng tiền đơn theo so.created_at trong khoảng và cùng bộ lọc vendor/status/provider. */
    public BigDecimal sumOrderTotal(LocalDate from, LocalDate toExclusive,
                                    Long vendorId, String status, String provider) {
        String where = " WHERE so.created_at >= :from AND so.created_at < :to ";
        Map<String, Object> params = new HashMap<>();
        params.put("from", ts(from));
        params.put("to", ts(toExclusive));

        if (vendorId != null) { where += " AND so.vendor_id = :vendorId"; params.put("vendorId", vendorId); }
        if (status != null && !status.isBlank()) { where += " AND so.status = :status"; params.put("status", status); }

        boolean filterProvider = provider != null && !provider.isBlank();
        String join = filterProvider
                ? " LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id=so.id AND p.status='PAID' AND p.provider=:provider "
                : " ";

        String sql = "SELECT COALESCE(SUM(so.total),0) FROM service_order so " + join + where;
        Query q = em.createNativeQuery(sql);
        params.forEach(q::setParameter);
        if (filterProvider) q.setParameter("provider", provider);
        return toBD(q.getSingleResult());
    }

    /** Đã thanh toán = SUM(payment.amount) theo p.paid_at và cùng bộ lọc vendor/status/provider. */
    public BigDecimal sumPaid(LocalDate from, LocalDate toExclusive,
                              Long vendorId, String status, String provider) {
        String where = """
            WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
              AND p.paid_at >= :from AND p.paid_at < :to
        """;
        Map<String, Object> params = new HashMap<>();
        params.put("from", ts(from));
        params.put("to", ts(toExclusive));

        if (vendorId != null) { where += " AND so.vendor_id = :vendorId"; params.put("vendorId", vendorId); }
        if (status != null && !status.isBlank()) { where += " AND so.status = :status"; params.put("status", status); }
        if (provider != null && !provider.isBlank()) { where += " AND p.provider = :provider"; params.put("provider", provider); }

        String sql = """
            SELECT COALESCE(SUM(p.amount),0)
            FROM payment p
            JOIN service_order so ON so.id = p.pay_target_id
            """ + where;
        Query q = em.createNativeQuery(sql);
        params.forEach(q::setParameter);
        return toBD(q.getSingleResult());
    }

    public static BigDecimal siteIncome(BigDecimal base) {
        return base == null ? BigDecimal.ZERO : base.multiply(BigDecimal.valueOf(0.15));
    }

    private static Timestamp ts(LocalDate d){ return Timestamp.valueOf(d.atStartOfDay()); }
    private static BigDecimal toBD(Object o){
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }
}
