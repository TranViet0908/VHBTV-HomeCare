// src/main/java/Project/HouseService/Controller/Admin/ReviewServiceAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.ReviewServiceAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import Project.HouseService.Entity.VendorServiceReview;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/reviews/service")
public class ReviewServiceAdminController {

    private final ReviewServiceAdminService service;

    public ReviewServiceAdminController(ReviewServiceAdminService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(required=false) String kw,
                       @RequestParam(required=false) Long vendorId,
                       @RequestParam(required=false) Long vendorServiceId,
                       @RequestParam(required=false) Boolean hidden,
                       @RequestParam(defaultValue="0") int page,
                       @RequestParam(defaultValue="12") int size,
                       Model model) {
        Page<VendorServiceReview> data = service.list(kw, vendorId, vendorServiceId, hidden, page, size);
        model.addAttribute("page", data);
        model.addAttribute("kw", kw);
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("vendorServiceId", vendorServiceId);
        model.addAttribute("hidden", hidden);
        return "admin/reviews/service/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("review", service.get(id));
        return "admin/reviews/service/detail";
    }

    @PostMapping("/{id}/hide")
    public String hide(@PathVariable Long id,
                       @RequestParam(required=false) String reason,
                       @SessionAttribute("ADMIN_ID") Long adminId,
                       RedirectAttributes ra) {
        service.hide(id, adminId, reason);
        ra.addFlashAttribute("success","Đã ẩn bình luận");
        return "redirect:/admin/reviews/service";
    }

    @PostMapping("/{id}/unhide")
    public String unhide(@PathVariable Long id, RedirectAttributes ra) {
        service.unhide(id);
        ra.addFlashAttribute("success","Đã hiện bình luận");
        return "redirect:/admin/reviews/service";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success","Đã xóa bình luận");
        return "redirect:/admin/reviews/service";
    }
}
