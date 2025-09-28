// src/main/java/Project/HouseService/Controller/Admin/VendorApplicationsController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Entity.VendorApplication.Status;
import Project.HouseService.Service.Admin.VendorApprovalService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/vendor-applications")
public class VendorApplicationsController {

    private final VendorApprovalService service;

    public VendorApplicationsController(VendorApprovalService service) {
        this.service = service;
    }

    // GET page list
    @GetMapping
    public String list(@RequestParam(required = false) Status status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String msg,
                       @RequestParam(required = false) String err,
                       Model model) {
        Page<VendorApplication> apps = service.list(status, page, size);
        model.addAttribute("apps", apps.getContent());
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        model.addAttribute("totalPages", apps.getTotalPages());
        model.addAttribute("statusFilter", status);
        model.addAttribute("msg", msg);
        model.addAttribute("err", err);
        return "admin/vendor-applications/list";
    }

    // APPROVE
    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id) {
        try {
            service.approve(id);
            return "redirect:/admin/vendor-applications?msg=approved";
        } catch (Exception e) {
            return "redirect:/admin/vendor-applications?err=approve";
        }
    }

    // REJECT
    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id) {
        try {
            service.reject(id);
            return "redirect:/admin/vendor-applications?msg=rejected";
        } catch (Exception e) {
            return "redirect:/admin/vendor-applications?err=reject";
        }
    }

    // DELETE
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return "redirect:/admin/vendor-applications?msg=deleted";
        } catch (Exception e) {
            return "redirect:/admin/vendor-applications?err=delete";
        }
    }
}
