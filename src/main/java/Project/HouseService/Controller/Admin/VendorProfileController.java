// src/main/java/Project/HouseService/Controller/Admin/VendorProfileController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.VendorProfileService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller("adminVendorProfileController")
@RequestMapping("/admin/vendors")
@PreAuthorize("hasRole('ADMIN')")
public class VendorProfileController {

    private final VendorProfileService service;

    public VendorProfileController(VendorProfileService service) {
        this.service = service;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) Boolean verified,
                       Model model) {
        model.addAttribute("q", q);
        model.addAttribute("verified", verified);
        model.addAttribute("page", service.pageFiltered(page, size, q, verified));
        return "admin/vendors/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("profile", service.findById(id));
        return "admin/vendors/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("profile", service.findById(id));
        return "admin/vendors/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @ModelAttribute("profile") Project.HouseService.Entity.VendorProfile form) {
        service.updateEditableFieldsByProfileId(id, form);
        return "redirect:/admin/vendors/" + id;
    }

    @PostMapping("/{id}/verify")
    public String verify(@PathVariable Long id,
                         @RequestParam(defaultValue = "true") boolean value,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(defaultValue = "12") int size) {
        service.setVerified(id, value);
        return "redirect:/admin/vendors?page=" + page + "&size=" + size;
    }
}
