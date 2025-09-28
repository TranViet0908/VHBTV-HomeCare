// src/main/java/Project/HouseService/Controller/Admin/ServiceAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.Service;
import Project.HouseService.Service.Admin.ServiceAdminService;
import Project.HouseService.Repository.ServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/services")
public class ServiceAdminController {

    private final ServiceAdminService app;
    private final ServiceRepository services;

    public ServiceAdminController(ServiceAdminService app, ServiceRepository services) {
        this.app = app;
        this.services = services;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String kw,
                       @RequestParam(required = false) Long parentId,
                       @RequestParam(required = false) String unit,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String sort,
                       Model model) {
        Page<Service> data = app.list(kw, parentId, unit, page, size, sort);
        model.addAttribute("data", data);
        model.addAttribute("kw", kw);
        model.addAttribute("parentId", parentId);
        model.addAttribute("unit", unit);
        model.addAttribute("sort", sort);
        model.addAttribute("size", size);
        model.addAttribute("allParents", services.findAll());
        return "admin/services/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("service", new Service());
        model.addAttribute("allParents", services.findAll());
        return "admin/services/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("service") Service form,
                         RedirectAttributes ra, Model model) {
        try {
            app.create(form);
            ra.addFlashAttribute("success", "Tạo danh mục thành công");
            return "redirect:/admin/services";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("allParents", services.findAll());
            return "admin/services/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        Service s = services.findById(id).orElse(null);
        if (s == null) {
            ra.addFlashAttribute("error", "Không tìm thấy dịch vụ");
            return "redirect:/admin/services";
        }
        model.addAttribute("service", s);
        model.addAttribute("allParents", services.findAll());
        return "admin/services/edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @ModelAttribute("service") Service form,
                       RedirectAttributes ra, Model model) {
        try {
            app.update(id, form);
            ra.addFlashAttribute("success", "Cập nhật thành công");
            return "redirect:/admin/services";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("allParents", services.findAll());
            return "admin/services/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            app.delete(id);
            ra.addFlashAttribute("success", "Đã xóa");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/services";
    }
}
