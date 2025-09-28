// src/main/java/Project/HouseService/Controller/Admin/CouponAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Service.Admin.CouponAdminService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/coupons")
public class CouponAdminController {

    private final CouponAdminService service;
    public CouponAdminController(CouponAdminService service) { this.service = service; }

    @GetMapping("/users")
    public String redirectUsers() { return "redirect:/admin/coupons?pick=users"; }

    @GetMapping("/vendors")
    public String redirectVendors() { return "redirect:/admin/coupons?pick=vendors"; }

    @GetMapping
    public String list(@RequestParam(value="q", required=false) String q,
                       @RequestParam(value="type", required=false) String type,
                       @RequestParam(value="active", required=false) Boolean active,
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
                       @RequestParam(value="from", required=false) LocalDateTime from,
                       @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
                       @RequestParam(value="to", required=false) LocalDateTime to,
                       @RequestParam(value="pick", required=false) String pick,
                       @RequestParam(value="page", defaultValue="0") int page,
                       @RequestParam(value="size", defaultValue="10") int size,
                       Model model){
        Page<Coupon> data = service.list(q, type, active, from, to, page, size);
        model.addAttribute("data", data);
        model.addAttribute("q", q);
        model.addAttribute("type", type);
        model.addAttribute("active", active);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("pick", pick);
        return "admin/coupons/list";
    }

    @GetMapping("/create")
    public String createForm(Model model){
        model.addAttribute("coupon", new Coupon());
        return "admin/coupons/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("coupon") Coupon coupon,
                         BindingResult br,
                         RedirectAttributes ra){
        if (br.hasErrors()) return "admin/coupons/create";
        service.create(coupon);
        ra.addFlashAttribute("msg","Tạo mã thành công");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model){
        model.addAttribute("coupon", service.require(id));
        return "admin/coupons/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model){
        model.addAttribute("coupon", service.require(id));
        return "admin/coupons/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @ModelAttribute("coupon") Coupon form,
                       BindingResult br,
                       RedirectAttributes ra){
        if (br.hasErrors()) return "admin/coupons/edit";
        service.update(id, form);
        ra.addFlashAttribute("msg","Cập nhật mã thành công");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id, RedirectAttributes ra){
        service.toggle(id);
        ra.addFlashAttribute("msg","Đã đổi trạng thái");
        return "redirect:/admin/coupons";
    }

    @PostMapping("/preview")
    public String preview(@RequestParam String code,
                          @RequestParam BigDecimal subtotal,
                          @RequestParam(required = false) Long userId,
                          @RequestParam(required = false) Long vendorId,
                          Model model){
        var pr = service.preview(code, subtotal, userId, vendorId);
        model.addAttribute("preview", pr);
        model.addAttribute("coupon", new Coupon());
        return "admin/coupons/create";
    }
}
