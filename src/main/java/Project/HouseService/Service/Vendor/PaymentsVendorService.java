// src/main/java/Project/HouseService/Service/Vendor/PaymentsVendorService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Repository.PaymentRepository;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentsVendorService {

    private final PaymentRepository paymentRepo;
    private final ServiceOrderRepository orderRepo;
    private final ServiceOrderItemRepository itemRepo;

    public PaymentsVendorService(PaymentRepository paymentRepo,
                                 ServiceOrderRepository orderRepo,
                                 ServiceOrderItemRepository itemRepo) {
        this.paymentRepo = paymentRepo;
        this.orderRepo = orderRepo;
        this.itemRepo = itemRepo;
    }

    /** vendorId = vendor_profile.user_id */
    public Map<String, Object> getSummary(Long vendorId, LocalDateTime from, LocalDateTime to) {
        Map<String, Object> m = new HashMap<>();

        // 1) Tổng tiền đã nhận (Payment.PAID)
        BigDecimal totalPaid = nz(paymentRepo.totalPaidForVendor(vendorId, from, to));

        // 2) Tổng hợp từ ServiceOrder (trả List<Object[]>)
        List<Object[]> rows = orderRepo.sumOrderAmounts(vendorId, from, to);
        Object[] r = (rows == null || rows.isEmpty())
                ? new Object[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO}
                : rows.get(0);
        BigDecimal sumSubtotal = toDecimal(r[0]);
        BigDecimal sumDiscount = toDecimal(r[1]);
        BigDecimal sumTotal    = toDecimal(r[2]);

        // 3) Breakdown + series
        List<Object[]> byProvider = nvlList(paymentRepo.sumByProviderForVendor(vendorId, from, to));
        List<Object[]> byStatus   = nvlList(paymentRepo.sumByStatusForVendor(vendorId, from, to));
        List<Object[]> dailyRaw   = nvlList(paymentRepo.dailyPaidSeriesForVendor(vendorId, from, to));
        List<Object[]> topSvcs    = nvlList(itemRepo.topServicesByRevenue(vendorId, from, to, 10));

        LocalDate fromD = from.toLocalDate();
        LocalDate toD   = to.toLocalDate();
        List<Object[]> daily = fillMissingDates(dailyRaw, fromD, toD);

        // Fallback: nếu series toàn 0, tự nhóm theo ngày từ danh sách Payment.PAID
        boolean allZero = daily.stream().allMatch(x -> toDecimal(x[1]).signum() == 0);
        if (allZero) {
            Map<LocalDate, BigDecimal> map = new HashMap<>();
            for (Payment p : paymentRepo.findByVendorBetween(vendorId, from, to, null, Payment.PaymentStatus.PAID)) {
                if (p.getPaidAt() == null) continue;
                LocalDate d = p.getPaidAt().toLocalDate();
                map.put(d, map.getOrDefault(d, BigDecimal.ZERO).add(nz(p.getAmount())));
            }
            daily = new ArrayList<>();
            for (LocalDate d = fromD; !d.isAfter(toD); d = d.plusDays(1)) {
                daily.add(new Object[]{ d, map.getOrDefault(d, BigDecimal.ZERO) });
            }
        }

        // 4) Model
        m.put("totalPaid", totalPaid);
        m.put("sumSubtotal", sumSubtotal);
        m.put("sumDiscount", sumDiscount);
        m.put("sumTotal", sumTotal);
        m.put("byProvider", byProvider);
        m.put("byStatus", byStatus);
        m.put("daily", daily);
        m.put("topServices", topSvcs);
        m.put("svgLine", buildSvgLine(daily));
        m.put("svgBars", buildSvgBars(byProvider));
        return m;
    }

    // Helper an toàn cho null list
    private <T> List<T> nvlList(List<T> in) {
        return in == null ? java.util.Collections.emptyList() : in;
    }


    /** Bảng chi tiết giao dịch */
    public List<Payment> listPayments(Long vendorId, LocalDateTime from, LocalDateTime to,
                                      Payment.Provider provider, Payment.PaymentStatus status) {
        return paymentRepo.findByVendorBetween(vendorId, from, to, provider, status);
    }

    // ================= Helpers =================

    private BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    private BigDecimal toDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        if (v instanceof BigDecimal bd) return bd;
        if (v instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(v.toString());
    }

    private List<Object[]> fillMissingDates(List<Object[]> dailyRaw, LocalDate from, LocalDate to) {
        Map<LocalDate, BigDecimal> map = new HashMap<>();
        for (Object[] row : dailyRaw) {
            LocalDate d = (row[0] instanceof LocalDate) ? (LocalDate) row[0]
                    : LocalDate.parse(String.valueOf(row[0]));
            map.put(d, toDecimal(row[1]));
        }
        List<Object[]> out = new ArrayList<>();
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            out.add(new Object[]{ d, map.getOrDefault(d, BigDecimal.ZERO) });
        }
        return out;
    }

    /** SVG đường: doanh thu theo ngày */
    public String buildSvgLine(List<Object[]> series) {
        int width = 820, height = 260, padL = 50, padB = 30, padT = 10, padR = 10;
        int plotW = width - padL - padR, plotH = height - padT - padB;

        double maxVal = 1.0;
        for (Object[] r : series) {
            double v = toDecimal(r[1]).doubleValue();
            if (v > maxVal) maxVal = v;
        }
        int n = Math.max(1, series.size());

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < n; i++) {
            double x = padL + (plotW * 1.0 * i / Math.max(1, n - 1));
            double y = padT + plotH * (1.0 - toDecimal(series.get(i)[1]).doubleValue() / maxVal);
            path.append(i == 0 ? "M" : " L").append(String.format(java.util.Locale.US, "%.1f,%.1f", x, y));
        }

        StringBuilder ticks = new StringBuilder();
        for (int t = 0; t <= 2; t++) {
            double y = padT + plotH * (t / 2.0);
            double v = maxVal * (1 - t / 2.0);
            ticks.append(String.format(java.util.Locale.US,
                    "<text x='%d' y='%.1f' font-size='10' text-anchor='end'>%.0f</text>",
                    padL - 6, y + 3, v));
            ticks.append(String.format(java.util.Locale.US,
                    "<line x1='%d' y1='%.1f' x2='%d' y2='%.1f' stroke='#e5e7eb'/>",
                    padL, y, width - padR, y));
        }

        return """
            <svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d'>
              <rect x='0' y='0' width='%d' height='%d' fill='white'/>
              %s
              <path d='%s' fill='none' stroke='#4f46e5' stroke-width='2'/>
              <line x1='%d' y1='%d' x2='%d' y2='%d' stroke='#111827'/>
              <line x1='%d' y1='%d' x2='%d' y2='%d' stroke='#111827'/>
            </svg>
        """.formatted(
                width, height, width, height,
                ticks.toString(),
                path.toString(),
                padL, padT, padL, padT + plotH,                  // y-axis
                padL, padT + plotH, padL + plotW, padT + plotH   // x-axis
        );
    }

    /** SVG cột: theo cổng thanh toán */
    public String buildSvgBars(List<Object[]> rows) {
        int width = 500, height = 260, pad = 40;
        int plotW = width - pad * 2, plotH = height - pad * 2;
        int n = Math.max(1, rows.size());
        double barW = plotW * 0.8 / n;
        double gap = (plotW * 0.2) / (n + 1);

        double maxVal = 1.0;
        for (Object[] r : rows) {
            double v = toDecimal(r[1]).doubleValue();
            if (v > maxVal) maxVal = v;
        }

        StringBuilder sb = new StringBuilder();
        double x = pad + gap;
        for (Object[] r : rows) {
            String label = String.valueOf(r[0]);
            double v = toDecimal(r[1]).doubleValue();
            double h = v <= 0 ? 0 : (plotH * v / maxVal);
            double y = pad + (plotH - h);
            sb.append(String.format(java.util.Locale.US,
                    "<rect x='%.1f' y='%.1f' width='%.1f' height='%.1f' fill='#10b981'/>", x, y, barW, h));
            sb.append(String.format(java.util.Locale.US,
                    "<text x='%.1f' y='%d' font-size='10' text-anchor='middle'>%s</text>",
                    x + barW / 2, height - pad + 12, label));
            x += barW + gap;
        }

        return """
            <svg xmlns='http://www.w3.org/2000/svg' width='%d' height='%d'>
              <rect x='0' y='0' width='%d' height='%d' fill='white'/>
              %s
              <line x1='%d' y1='%d' x2='%d' y2='%d' stroke='#111827'/>
              <line x1='%d' y1='%d' x2='%d' y2='%d' stroke='#111827'/>
            </svg>
        """.formatted(
                width, height, width, height,
                sb.toString(),
                pad, pad, pad, pad + plotH,                    // y-axis
                pad, pad + plotH, pad + plotW, pad + plotH     // x-axis
        );
    }
}
