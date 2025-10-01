// src/main/java/Project/HouseService/Controller/Vendor/VendorServicesController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.Service;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Service.Vendor.VendorServiceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/vendor/services")
public class VendorServicesController {

    private final VendorServiceService app;
    public VendorServicesController(VendorServiceService app) { this.app = app; }

    @GetMapping
    public String index(Model model) {
        Long vendorId = app.currentVendorId();
        List<VendorService> my = app.listMyServices(vendorId);
        model.addAttribute("nav", "services");
        model.addAttribute("myServices", my);
        return "vendor/services/index";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        Long vendorId = app.currentVendorId();
        List<Service> assignable = app.listAssignable(vendorId);
        model.addAttribute("nav", "services");
        model.addAttribute("assignableServices", assignable);
        return "vendor/services/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam("serviceId") Long serviceId,
                         @RequestParam(value = "basePrice", required = false) BigDecimal basePrice,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "cover", required = false) MultipartFile cover,
                         RedirectAttributes ra) {
        Long vendorId = app.currentVendorId();
        boolean ok = app.addFromCatalog(vendorId, serviceId, basePrice, title, cover);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Đã thêm dịch vụ." : "Bạn đã có dịch vụ này.");
        return "redirect:/vendor/services";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Long vendorId = app.currentVendorId();
        VendorService item = app.findOwned(vendorId, id);
        if (item == null) {
            ra.addFlashAttribute("error", "Không tìm thấy dịch vụ.");
            return "redirect:/vendor/services";
        }
        model.addAttribute("nav", "services");
        model.addAttribute("item", item);
        return "vendor/services/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @RequestParam(value = "title", required = false) String title,
                         @RequestParam(value = "basePrice", required = false) BigDecimal basePrice,
                         @RequestParam(value = "status", required = false) String status,
                         @RequestParam(value = "unit", required = false) String unit,
                         @RequestParam(value = "durationMinutes", required = false) Integer durationMinutes,
                         @RequestParam(value = "minNoticeHours", required = false) Integer minNoticeHours,
                         @RequestParam(value = "maxDailyJobs", required = false) Integer maxDailyJobs,
                         @RequestParam(value = "description", required = false) String description,
                         @RequestParam(value = "cover", required = false) MultipartFile cover,
                         RedirectAttributes ra) {
        Long vendorId = app.currentVendorId();
        boolean ok = app.updateFull(vendorId, id, title, basePrice, status, unit,
                durationMinutes, minNoticeHours, maxDailyJobs, description, cover);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Đã cập nhật dịch vụ." : "Không cập nhật được.");
        return "redirect:/vendor/services"; // quay về index
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        Long vendorId = app.currentVendorId();
        boolean ok = app.delete(vendorId, id);
        ra.addFlashAttribute(ok ? "success" : "error",
                ok ? "Đã xóa dịch vụ." : "Không xóa được.");
        return "redirect:/vendor/services";
    }
}
