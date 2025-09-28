package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.DashboardAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Controller
public class DashboardAdminController {

    private final DashboardAdminService service;

    public DashboardAdminController(DashboardAdminService service) {
        this.service = service;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        ZoneId zone = ZoneId.of("Asia/Bangkok");
        LocalDate today = LocalDate.now(zone);
        LocalDate from30 = today.minusDays(29);
        LocalDate to30 = today;

        // Lists: luôn khác null
        List<Object[]> revenue7d = nz(service.revenue7d(zone));
        List<Object[]> ordersByStatusMonth = nz(service.ordersByStatusMonth(zone));
        List<Object[]> revenueByProviderMonth = nz(service.revenueByProviderMonth(zone));
        List<Object[]> topVendors5 = nz(service.topVendors(from30, to30, 5));
        List<Object[]> topServices5 = nz(service.topServices(from30, to30, 5));

        // KPIs all-time
        BigDecimal totalRevenueAll = service.totalRevenueAll();
        long totalOrdersAll = service.totalOrdersAll();
        BigDecimal websiteIncomeAll = service.websiteIncomeAll();

        // KPIs month
        BigDecimal totalRevenueMonth = sumBD(revenueByProviderMonth, 2);
        long totalOrdersMonth = sumLong(ordersByStatusMonth, 1);
        BigDecimal siteIncomeMonthOnPaid = totalRevenueMonth.multiply(BigDecimal.valueOf(0.15));

        // Model attrs: tên khớp dashboard.html
        model.addAttribute("revenue7d", revenue7d);
        model.addAttribute("ordersByStatusMonth", ordersByStatusMonth);
        model.addAttribute("revenueByProviderMonth", revenueByProviderMonth);
        model.addAttribute("topVendors5", topVendors5);
        model.addAttribute("topServices5", topServices5);

        model.addAttribute("totalRevenueAll", totalRevenueAll);
        model.addAttribute("totalOrdersAll", totalOrdersAll);
        model.addAttribute("websiteIncomeAll", websiteIncomeAll);
        model.addAttribute("totalRevenueMonth", totalRevenueMonth);
        model.addAttribute("totalOrdersMonth", totalOrdersMonth);
        model.addAttribute("siteIncomeMonthOnPaid", siteIncomeMonthOnPaid);

        return "admin/dashboard";
    }

    /* helpers */
    private static List<Object[]> nz(List<Object[]> v){ return v == null ? Collections.emptyList() : v; }
    private static java.math.BigDecimal sumBD(List<Object[]> rows, int idx){
        java.math.BigDecimal s = java.math.BigDecimal.ZERO;
        for (Object[] r : rows) if (r != null && r.length>idx && r[idx]!=null)
            s = s.add(new java.math.BigDecimal(r[idx].toString()));
        return s;
    }
    private static long sumLong(List<Object[]> rows, int idx){
        long s = 0;
        for (Object[] r : rows) if (r != null && r.length>idx && r[idx]!=null)
            s += Long.parseLong(r[idx].toString());
        return s;
    }
}
