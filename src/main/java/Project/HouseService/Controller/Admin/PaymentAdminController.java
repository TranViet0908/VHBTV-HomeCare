// src/main/java/Project/HouseService/Controller/Admin/PaymentAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.PaymentAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/payments") // ← base path mới
public class PaymentAdminController {

    private final PaymentAdminService payments;

    public PaymentAdminController(PaymentAdminService payments) {
        this.payments = payments;
    }

    // GET /admin/payments
    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) String provider,
                       @RequestParam(required = false) String currency,
                       @RequestParam(required = false) LocalDateTime from,
                       @RequestParam(required = false) LocalDateTime to,
                       @RequestParam(required = false) BigDecimal minAmount,
                       @RequestParam(required = false) BigDecimal maxAmount,
                       @RequestParam(required = false) Long serviceOrderId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        Page<?> data = payments.search(
                q, status, provider, currency, from, to, minAmount, maxAmount, serviceOrderId,
                PageRequest.of(page, size)
        );

        model.addAttribute("page", data);
        model.addAttribute("q", q);
        model.addAttribute("status", status);
        model.addAttribute("provider", provider);
        model.addAttribute("currency", currency);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("serviceOrderId", serviceOrderId);

        return "admin/payments/list"; // ← khớp templates
    }

    // GET /admin/payments/{id}
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var payment = payments.getOrThrow(id);
        model.addAttribute("payment", payment);
        return "admin/payments/detail"; // ← khớp templates
    }

    // POST /admin/payments/{id}/mark-paid
    @PostMapping("/{id}/mark-paid")
    public String markPaid(@PathVariable Long id, RedirectAttributes ra) {
        payments.markPaid(id);
        ra.addFlashAttribute("success", "Đã đánh dấu thanh toán PAID.");
        return "redirect:/admin/payments/" + id; // ← redirect theo base mới
    }

    // POST /admin/payments/{id}/mark-refunded
    @PostMapping("/{id}/mark-refunded")
    public String markRefunded(@PathVariable Long id, RedirectAttributes ra) {
        payments.markRefunded(id);
        ra.addFlashAttribute("success", "Đã đánh dấu thanh toán REFUNDED.");
        return "redirect:/admin/payments/" + id; // ← redirect theo base mới
    }

    // GET /admin/payments/reconcile
    @GetMapping("/reconcile")
    public String reconcile(@RequestParam(required = false) LocalDateTime from,
                            @RequestParam(required = false) LocalDateTime to,
                            @RequestParam(required = false) String provider,
                            @RequestParam(required = false) String currency,
                            Model model) {

        var summary = payments.reconcileSummary(from, to, provider, currency);
        var rows = payments.reconcileByProvider(from, to, provider, currency);

        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("provider", provider);
        model.addAttribute("currency", currency);
        model.addAttribute("summary", summary);
        model.addAttribute("rows", rows);

        return "admin/payments/reconcile"; // ← khớp templates
    }
}
