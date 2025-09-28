package Project.HouseService.Service.Admin;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardAdminService {

    @PersistenceContext
    private EntityManager em;

    /* ====== Doanh thu & thu nhập: CHỐT THEO PAYMENT.PAID ====== */

    /** Tổng doanh thu đã thanh toán tất cả thời gian */
    public BigDecimal totalRevenueAll() {
        Object r = em.createNativeQuery("""
            SELECT COALESCE(SUM(p.amount),0)
            FROM payment p
            WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
        """).getSingleResult();
        return toBD(r);
    }

    /** Tổng đơn tất cả thời gian (đếm đơn, không phụ thuộc thanh toán) */
    public long totalOrdersAll() {
        Object r = em.createNativeQuery("SELECT COUNT(*) FROM service_order").getSingleResult();
        return ((Number) r).longValue();
    }

    /** Thu nhập website = 15% doanh thu đã thanh toán */
    public BigDecimal websiteIncomeAll() {
        return totalRevenueAll().multiply(BigDecimal.valueOf(0.15));
    }

    /* ====== 7 ngày gần nhất: theo ngày thanh toán (paid_at) ====== */
    /** row = [LocalDate day, BigDecimal revenuePaid, Long paymentsCount, BigDecimal noop]  */
    public List<Object[]> revenue7d(ZoneId zone) {
        LocalDate today = LocalDate.now(zone);
        LocalDate start = today.minusDays(6);
        LocalDate endExclusive = today.plusDays(1);

        String sql = """
            SELECT DATE(p.paid_at) AS d,
                   COALESCE(SUM(p.amount),0) AS revenue_paid,
                   COUNT(*) AS payments_cnt,
                   0 AS dummy
            FROM payment p
            WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
              AND p.paid_at >= :from AND p.paid_at < :to
            GROUP BY d
            ORDER BY d
        """;
        Query q = em.createNativeQuery(sql);
        q.setParameter("from", ts(start));
        q.setParameter("to", ts(endExclusive));

        @SuppressWarnings("unchecked")
        List<Object[]> raw = q.getResultList();

        var map = new java.util.HashMap<LocalDate, Object[]>();
        for (Object[] r : raw) {
            LocalDate d = ((Date) r[0]).toLocalDate();
            map.put(d, new Object[]{ d, toBD(r[1]), toLong(r[2]), BigDecimal.ZERO });
        }
        List<Object[]> out = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(today); d = d.plusDays(1)) {
            out.add(map.getOrDefault(d, new Object[]{ d, BigDecimal.ZERO, 0L, BigDecimal.ZERO }));
        }
        return out;
    }

    /* ====== Tháng hiện tại ====== */

    /** row = [String status, Long count] — vẫn thống kê theo đơn */
    public List<Object[]> ordersByStatusMonth(ZoneId zone) {
        LocalDate from = LocalDate.now(zone).withDayOfMonth(1);
        LocalDate to = from.plusMonths(1);
        Query q = em.createNativeQuery("""
            SELECT so.status, COUNT(*) AS cnt
            FROM service_order so
            WHERE so.created_at >= :from AND so.created_at < :to
            GROUP BY so.status
            ORDER BY cnt DESC
        """);
        q.setParameter("from", ts(from));
        q.setParameter("to", ts(to));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows == null ? List.of() : rows;
    }

    /** row = [String provider, Long payments, BigDecimal amountPaid] — chỉ tính PAID và theo paid_at */
    public List<Object[]> revenueByProviderMonth(ZoneId zone) {
        LocalDate from = LocalDate.now(zone).withDayOfMonth(1);
        LocalDate to = from.plusMonths(1);
        Query q = em.createNativeQuery("""
            SELECT p.provider,
                   COUNT(*) AS payments,
                   COALESCE(SUM(p.amount),0) AS amountPaid
            FROM payment p
            WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
              AND p.paid_at >= :from AND p.paid_at < :to
            GROUP BY p.provider
            ORDER BY amountPaid DESC
        """);
        q.setParameter("from", ts(from));
        q.setParameter("to", ts(to));
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows == null ? List.of() : rows;
    }

    /** Top vendors theo doanh thu đã thanh toán trong khoảng [from, to] (to inclusive).
     * Trả về: [vendor_id, vendor_name, revenue_paid(BigDecimal), payments_cnt(Long)]
     * Sắp xếp cuối cùng: revenue_paid DESC, payments_cnt DESC, vendor_name ASC
     */
    public List<Object[]> topVendors(LocalDate from, LocalDate to, int limit) {
        String sql = """
        SELECT
            so.vendor_id                                                   AS vendor_id,
            COALESCE(MAX(vp.display_name), CONCAT('User #', so.vendor_id)) AS vendor_name,
            COALESCE(SUM(p.amount),0)                                      AS revenue_paid,
            COUNT(*)                                                       AS payments_cnt
        FROM payment p
        JOIN service_order so ON so.id = p.pay_target_id
        LEFT JOIN vendor_profile vp ON vp.user_id = so.vendor_id
        WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
          AND p.paid_at >= :from AND p.paid_at < :toEx
        GROUP BY so.vendor_id
        ORDER BY payments_cnt DESC, revenue_paid DESC, vendor_name ASC
    """;
        var q = em.createNativeQuery(sql);
        q.setParameter("from", java.sql.Timestamp.valueOf(from.atStartOfDay()));
        q.setParameter("toEx", java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        q.setMaxResults(limit);
        @SuppressWarnings("unchecked") var rows = (java.util.List<Object[]>) q.getResultList();
        return rows == null ? java.util.List.of() : rows;
    }

    /** Top services theo các đơn đã thanh toán trong [from, to] (to inclusive).
     * Trả về: [service_id, title, items_paid(Long), revenue_paid(BigDecimal)]
     * Template dùng: service[1] = title, service[2] = lượt đặt.
     */
    public List<Object[]> topServices(LocalDate from, LocalDate to, int limit) {
        String sql = """
        SELECT
            vs.id                                   AS service_id,
            MAX(vs.title)                           AS title,
            COUNT(*)                                AS items_paid,
            COALESCE(SUM(soi.subtotal),0)           AS revenue_paid
        FROM service_order_item soi
        JOIN service_order so ON so.id = soi.service_order_id
        JOIN vendor_service vs ON vs.id = soi.vendor_service_id
        WHERE EXISTS (
            SELECT 1 FROM payment p
            WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
              AND p.pay_target_id = so.id
              AND p.paid_at >= :from AND p.paid_at < :toEx
        )
        GROUP BY vs.id
        ORDER BY items_paid DESC, revenue_paid DESC, title COLLATE utf8mb4_unicode_ci ASC
    """;
        var q = em.createNativeQuery(sql);
        q.setParameter("from", java.sql.Timestamp.valueOf(from.atStartOfDay()));
        q.setParameter("toEx", java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
        q.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        return rows == null ? List.of() : rows;
    }

    /* helpers */
    private static Timestamp tsStart(LocalDate d){ return Timestamp.valueOf(d.atStartOfDay()); }
    private static BigDecimal toBD(Object o){
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        return new BigDecimal(o.toString());
    }
    private static long toLong(Object o){
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        return Long.parseLong(o.toString());
    }
    private static Timestamp ts(LocalDate d){ return Timestamp.valueOf(d.atStartOfDay()); }
}
