// src/main/java/Project/HouseService/Service/Vendor/VendorDashboardService.java
package Project.HouseService.Service.Vendor;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

@Service
public class VendorDashboardService {

    private final NamedParameterJdbcTemplate jdbc;
    public VendorDashboardService(NamedParameterJdbcTemplate jdbc) { this.jdbc = jdbc; }

    // (giữ nguyên getUserByPrincipal)

    // đơn giản: chỉ lấy thông tin hồ sơ, KHÔNG lấy rating ở đây nữa
    public Map<String, Object> getVendorProfile(long vendorUserId) {
        var sql = """
            SELECT display_name, legal_name, verified, address_line
            FROM vendor_profile
            WHERE user_id = :vid
            LIMIT 1
        """;
        return jdbc.query(sql,
                new MapSqlParameterSource("vid", vendorUserId),
                rs -> rs.next() ? Map.of(
                        "displayName", rs.getString("display_name"),
                        "legalName", rs.getString("legal_name"),
                        "verified", rs.getObject("verified") != null && rs.getBoolean("verified"),
                        "address", rs.getString("address_line")
                ) : Collections.emptyMap());
    }

    // NEW: tính rating thực từ 2 bảng review và không lấy bản bị ẩn
    private Map<String, Object> getRatingStats(long vendorUserId) {
        var p = new MapSqlParameterSource("vid", vendorUserId);
        return jdbc.query("""
            SELECT COALESCE(AVG(r),0) AS avg_rating, COUNT(*) AS cnt FROM (
                SELECT CAST(vr.rating AS DECIMAL(10,2)) AS r
                FROM vendor_review vr
                WHERE vr.vendor_id = :vid AND COALESCE(vr.hidden,0) = 0
                UNION ALL
                SELECT CAST(vsr.rating AS DECIMAL(10,2)) AS r
                FROM vendor_service_review vsr
                WHERE vsr.vendor_id = :vid AND COALESCE(vsr.hidden,0) = 0
            ) x
        """, p, rs -> {
            if (rs.next()) {
                return Map.of(
                        "avg", rs.getBigDecimal("avg_rating"),
                        "cnt", rs.getLong("cnt")
                );
            }
            return Map.of("avg", new BigDecimal("0.00"), "cnt", 0L);
        });
    }

    private long count(String sql, MapSqlParameterSource params) {
        Long v = jdbc.query(sql, params, rs -> rs.next() ? rs.getLong(1) : 0L);
        return v == null ? 0L : v;
    }

    public void fillDashboardModel(long vendorUserId, Model model) {
        // Thông tin cơ bản
        Map<String, Object> vp = getVendorProfile(vendorUserId);
        String greetingName =
                Optional.ofNullable((String) vp.get("displayName")).filter(s->!s.isBlank())
                        .orElse(Optional.ofNullable((String) vp.get("legalName")).filter(s->!s.isBlank()).orElse("Vendor"));

        var p = new MapSqlParameterSource("vid", vendorUserId);

        // Đơn theo trạng thái (giữ nguyên)
        long ordersPending   = count("SELECT COUNT(*) FROM service_order WHERE vendor_id=:vid AND status='PENDING'", p);
        long ordersConfirmed = count("SELECT COUNT(*) FROM service_order WHERE vendor_id=:vid AND status='CONFIRMED'", p);
        long ordersInProgress= count("SELECT COUNT(*) FROM service_order WHERE vendor_id=:vid AND status='IN_PROGRESS'", p);
        long ordersCompleted = count("SELECT COUNT(*) FROM service_order WHERE vendor_id=:vid AND status='COMPLETED'", p);
        long ordersCancelled = count("SELECT COUNT(*) FROM service_order WHERE vendor_id=:vid AND status='CANCELLED'", p);
        long ordersOpen = ordersPending + ordersConfirmed + ordersInProgress;

        // Lịch hôm nay
        long todayJobs = count("""
            SELECT COUNT(*) FROM service_order_item
            WHERE vendor_id=:vid AND DATE(scheduled_at)=CURRENT_DATE()
        """, p);

        // Dịch vụ đang hoạt động
        long servicesActive = count("""
            SELECT COUNT(*) FROM vendor_service
            WHERE vendor_id=:vid AND status='ACTIVE'
        """, p);

        // Doanh thu tháng này
        BigDecimal revenueMonth = jdbc.query("""
            SELECT COALESCE(SUM(p.amount),0) AS money
            FROM payment p
            JOIN service_order so ON so.id = p.pay_target_id AND p.pay_target_type='SERVICE_ORDER'
            WHERE so.vendor_id=:vid AND p.status='PAID'
              AND YEAR(p.paid_at)=YEAR(CURRENT_DATE()) AND MONTH(p.paid_at)=MONTH(CURRENT_DATE())
        """, p, rs -> rs.next() ? rs.getBigDecimal("money") : BigDecimal.ZERO);
        NumberFormat vn = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String revenueMonthText = vn.format(revenueMonth);

        // Đơn gần đây (giữ nguyên)
        List<Map<String, Object>> recentOrders = jdbc.query("""
            SELECT id, order_code, status, total, created_at
            FROM service_order
            WHERE vendor_id=:vid
            ORDER BY created_at DESC
            LIMIT 5
        """, p, (rs, i) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("order_code", rs.getString("order_code"));
            row.put("status", rs.getString("status"));
            row.put("total_text", vn.format(rs.getBigDecimal("total")));
            row.put("created_at", rs.getTimestamp("created_at"));

            String st = rs.getString("status");
            String statusClass =
                    "PENDING".equals(st) ? " status-badge-pending" :
                            ("CONFIRMED".equals(st) || "IN_PROGRESS".equals(st)) ? " status-badge-confirmed" :
                                    "COMPLETED".equals(st) ? " status-badge-completed" : " status-badge-cancelled";
            row.put("statusClass", statusClass);
            return row;
        });

        // Rating thực
        Map<String, Object> r = getRatingStats(vendorUserId);
        BigDecimal ratingAvg = (BigDecimal) r.get("avg");
        long ratingCount = (long) r.get("cnt");
        boolean verified = (boolean) vp.getOrDefault("verified", false);

        // Push model
        model.addAttribute("greetingName", greetingName);
        model.addAttribute("todayJobs", todayJobs);
        model.addAttribute("ratingAvg", ratingAvg);
        model.addAttribute("ratingCount", ratingCount);
        model.addAttribute("vendorVerified", verified);

        model.addAttribute("ordersOpen", ordersOpen);
        model.addAttribute("ordersPending", ordersPending);
        model.addAttribute("ordersConfirmed", ordersConfirmed);
        model.addAttribute("ordersInProgress", ordersInProgress);
        model.addAttribute("ordersCompleted", ordersCompleted);
        model.addAttribute("ordersCancelled", ordersCancelled);

        model.addAttribute("servicesActive", servicesActive);
        model.addAttribute("revenueMonthText", revenueMonthText);
        model.addAttribute("recentOrders", recentOrders);

        model.addAttribute("vendorProfile", vp);
    }
}