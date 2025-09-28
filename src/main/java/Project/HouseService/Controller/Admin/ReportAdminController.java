package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.ReportAdminService;
import Project.HouseService.Service.Admin.ReportAdminService.PageSlice;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Locale;

@Controller
public class ReportAdminController {

    private final ReportAdminService svc;
    private final SpringTemplateEngine templateEngine;
    private final ServletContext servletContext;

    public ReportAdminController(ReportAdminService svc,
                                 SpringTemplateEngine templateEngine,
                                 ServletContext servletContext) {
        this.svc = svc;
        this.templateEngine = templateEngine;
        this.servletContext = servletContext;
    }

    @GetMapping({"/admin/reports", "/admin/reports/", "/admin/reports/index", "/admin/reports/index.html"})
    public String index(Model model,
                        @RequestParam(required = false) String from,
                        @RequestParam(required = false) String to,
                        @RequestParam(required = false) Long vendorId,
                        @RequestParam(required = false) String status,
                        @RequestParam(required = false) String provider,
                        @RequestParam(required = false) Integer page,
                        @RequestParam(required = false) Integer size) {

        ZoneId zone = ZoneId.of("Asia/Bangkok");
        LocalDate monthStart = LocalDate.now(zone).withDayOfMonth(1);

        LocalDate fromD = (from == null || from.isBlank()) ? monthStart : LocalDate.parse(from);
        LocalDate toD   = (to == null   || to.isBlank())   ? monthStart.plusMonths(1).minusDays(1) : LocalDate.parse(to);
        LocalDate toExclusive = toD.plusDays(1);

        int p = (page == null || page < 0) ? 0 : page;
        int s = (size == null || size <= 0) ? 10 : size;

        PageSlice<Object[]> slice = svc.orderReport(fromD, toExclusive, vendorId, status, provider, p, s);

        BigDecimal sumTotal = svc.sumOrderTotal(fromD, toExclusive, vendorId, status, provider);
        BigDecimal sumPaid  = svc.sumPaid(fromD, toExclusive, vendorId, status, provider);
        long sumOrders = slice.total;

        model.addAttribute("from", fromD);
        model.addAttribute("to", toD);

        model.addAttribute("vendorId", vendorId);
        model.addAttribute("status", status);
        model.addAttribute("provider", provider);

        model.addAttribute("sumOrders", sumOrders);
        model.addAttribute("sumTotal", sumTotal);
        model.addAttribute("sumPaid",  sumPaid);
        model.addAttribute("siteIncomeOrders", ReportAdminService.siteIncome(sumTotal));
        model.addAttribute("siteIncomePaid",   ReportAdminService.siteIncome(sumPaid));

        model.addAttribute("rows", slice.content);
        model.addAttribute("total", slice.total);
        model.addAttribute("page", p);
        model.addAttribute("size", s);

        return "admin/reports/index";
    }

    @GetMapping(
            value = {"/admin/reports/export", "/admin/reports/export.pdf"},
            produces = "application/pdf"
    )
    public void exportPdf(HttpServletResponse response,
                          @RequestParam(required = false) String from,
                          @RequestParam(required = false) String to,
                          @RequestParam(required = false) Long vendorId,
                          @RequestParam(required = false) String status,
                          @RequestParam(required = false) String provider) throws Exception {
        ZoneId zone = ZoneId.of("Asia/Bangkok");
        LocalDate monthStart = LocalDate.now(zone).withDayOfMonth(1);

        LocalDate fromD = (from == null || from.isBlank()) ? monthStart : LocalDate.parse(from);
        LocalDate toD   = (to == null   || to.isBlank())   ? monthStart.plusMonths(1).minusDays(1) : LocalDate.parse(to);
        LocalDate toExclusive = toD.plusDays(1);

        // Lấy toàn bộ dòng để in PDF
        PageSlice<Object[]> slice = svc.orderReport(fromD, toExclusive, vendorId, status, provider, 0, 100000);

        BigDecimal sumTotal = svc.sumOrderTotal(fromD, toExclusive, vendorId, status, provider);
        BigDecimal sumPaid  = svc.sumPaid(fromD, toExclusive, vendorId, status, provider);

        Context ctx = new Context(Locale.forLanguageTag("vi"));
        ctx.setVariable("from", fromD);
        ctx.setVariable("to", toD);
        ctx.setVariable("vendorId", vendorId);
        ctx.setVariable("status", status);
        ctx.setVariable("provider", provider);

        ctx.setVariable("sumOrders", slice.total);
        ctx.setVariable("sumTotal", sumTotal);
        ctx.setVariable("sumPaid",  sumPaid);
        ctx.setVariable("siteIncomeOrders", ReportAdminService.siteIncome(sumTotal));
        ctx.setVariable("siteIncomePaid",   ReportAdminService.siteIncome(sumPaid));

        ctx.setVariable("rows", slice.content);
        ctx.setVariable("total", slice.total);

        // Render Thymeleaf → HTML
        String html = templateEngine.process("admin/reports/pdf", ctx);

        // Base URI cho tài nguyên tương đối nếu cần
        URL base = servletContext.getResource("/");

        // Xuất PDF
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/pdf");
        String fname = "report-" + fromD.getYear() + "-" + String.format("%02d", fromD.getMonthValue()) + ".pdf";
        response.setHeader("Content-Disposition", "inline; filename=" + fname);

        try (OutputStream os = response.getOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, base != null ? base.toString() : null);
            builder.toStream(os);
            builder.run();
        }
    }
}
