// src/main/java/Project/HouseService/Controller/Vendor/PaymentsVendorController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Service.Vendor.PaymentsVendorService;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.svgsupport.BatikSVGDrawer;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/vendor/payments")
public class PaymentsVendorController {

    private final PaymentsVendorService service;
    private final VendorProfileRepository vendorProfileRepo;
    private final TemplateEngine templateEngine;

    public PaymentsVendorController(PaymentsVendorService service,
                                    VendorProfileRepository vendorProfileRepo,
                                    TemplateEngine templateEngine) {
        this.service = service;
        this.vendorProfileRepo = vendorProfileRepo;
        this.templateEngine = templateEngine;
    }

    private Long currentVendorUserId(Authentication auth) {
        String username = auth.getName();
        VendorProfile vp = vendorProfileRepo.findByUser_Username(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Vendor profile not found"));
        if (vp.getUser() == null) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vendor user not found");
        return vp.getUser().getId();
    }

    @GetMapping
    public String index(Authentication auth,
                        @RequestParam(required = false) String from,
                        @RequestParam(required = false) String to,
                        @RequestParam(required = false) Payment.Provider provider,
                        @RequestParam(required = false) Payment.PaymentStatus status,
                        Model model) {

        ZoneId zone = ZoneId.of("Asia/Bangkok");
        LocalDate toD = (to == null || to.isBlank()) ? LocalDate.now(zone) : LocalDate.parse(to);
        LocalDate fromD = (from == null || from.isBlank()) ? toD.minusDays(29) : LocalDate.parse(from);
        LocalDateTime fromTs = fromD.atStartOfDay();
        LocalDateTime toTs = toD.atTime(23, 59, 59);

        Long vendorId = currentVendorUserId(auth);
        Map<String, Object> summary = service.getSummary(vendorId, fromTs, toTs);
        List<Payment> payments = service.listPayments(vendorId, fromTs, toTs, provider, status);

        model.addAttribute("nav", "payments");
        model.addAttribute("from", fromD);
        model.addAttribute("to", toD);
        model.addAttribute("provider", provider);
        model.addAttribute("status", status);
        model.addAttribute("summary", summary);
        model.addAttribute("payments", payments);
        return "vendor/payments/index";
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(Authentication auth,
                                            @RequestParam String from,
                                            @RequestParam String to) {
        LocalDate fromD = LocalDate.parse(from);
        LocalDate toD   = LocalDate.parse(to);
        LocalDateTime fromTs = fromD.atStartOfDay();
        LocalDateTime toTs   = toD.atTime(23, 59, 59);

        Long vendorId = currentVendorUserId(auth);
        Map<String, Object> summary = service.getSummary(vendorId, fromTs, toTs);

        Context ctx = new Context(java.util.Locale.forLanguageTag("vi"));
        ctx.setVariable("from", fromD);
        ctx.setVariable("to", toD);
        ctx.setVariable("summary", summary);

        String html = templateEngine.process("vendor/payments/report", ctx);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();

            // Bật SVG
            builder.useSVGDrawer(new BatikSVGDrawer());

            // Đăng ký font: ưu tiên classpath, fallback hệ điều hành
            registerFonts(builder);

            builder.withHtmlContent(html, null);
            builder.toStream(out);
            builder.run();

            String filename = "payments-" + DateTimeFormatter.BASIC_ISO_DATE.format(fromD)
                    + "-" + DateTimeFormatter.BASIC_ISO_DATE.format(toD) + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(ContentDisposition.attachment()
                    .filename(filename, StandardCharsets.UTF_8).build());
            return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
        } catch (Exception e) {
            throw new RuntimeException("Export PDF error: " + e.getMessage(), e);
        }
    }

    private void registerFonts(PdfRendererBuilder b) throws IOException {
        // 1) Classpath (nếu sau này bạn thêm fonts/NotoSans-*.ttf vào resources)
        addClasspathFont(b, "/fonts/NotoSans-Regular.ttf", "VHBTVPDF", 400);
        addClasspathFont(b, "/fonts/NotoSans-Bold.ttf",    "VHBTVPDF", 700);

        // 2) Windows
        addFsFont(b, "C:/Windows/Fonts/segoeui.ttf",  "VHBTVPDF", 400);
        addFsFont(b, "C:/Windows/Fonts/segoeuib.ttf", "VHBTVPDF", 700);
        addFsFont(b, "C:/Windows/Fonts/arialuni.ttf", "VHBTVPDF", 400); // Arial Unicode MS (nếu có)

        // 3) Linux
        addFsFont(b, "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",      "VHBTVPDF", 400);
        addFsFont(b, "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf", "VHBTVPDF", 700);

        // 4) macOS
        addFsFont(b, "/Library/Fonts/Arial Unicode.ttf",     "VHBTVPDF", 400);
        addFsFont(b, "/Library/Fonts/Arial Unicode MS.ttf",  "VHBTVPDF", 400);
        addFsFont(b, "/Library/Fonts/HelveticaNeueDeskInterface.ttc", "VHBTVPDF", 400);
    }

    private void addClasspathFont(PdfRendererBuilder b, String cp, String family, int weight) {
        InputStream in = getClass().getResourceAsStream(cp);
        if (in != null) {
            b.useFont(() -> in, family, weight, PdfRendererBuilder.FontStyle.NORMAL, true);
        }
    }

    private void addFsFont(PdfRendererBuilder b, String path, String family, int weight) {
        File f = new File(path);
        if (f.exists()) {
            b.useFont(f, family, weight, PdfRendererBuilder.FontStyle.NORMAL, true);
        }
    }
}
