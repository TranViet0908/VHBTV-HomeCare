package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorSkill;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Service.Admin.VendorSkillAdminService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.constraints.*;
import java.util.List;

@Controller
@RequestMapping("/admin/vendor-skills")
@PreAuthorize("hasRole('ADMIN')")
public class VendorSkillAdminController {

    private final VendorSkillAdminService service;
    private final VendorProfileRepository vendorProfiles;

    public VendorSkillAdminController(VendorSkillAdminService service,
                                      VendorProfileRepository vendorProfiles) {
        this.service = service;
        this.vendorProfiles = vendorProfiles;
    }

    @GetMapping
    public String list(@RequestParam(value = "vendorId", required = false) Long vendorId,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {

        Page<VendorSkill> data = service.list(vendorId, q, page, size);
        List<VendorProfile> allVendors = vendorProfiles.findAll();

        model.addAttribute("page", data);
        model.addAttribute("q", q);
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("vendors", allVendors);
        return "admin/vendor_skills/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("skill", new VendorSkill());
        model.addAttribute("vendors", vendorProfiles.findAll());
        return "admin/vendor_skills/create";
    }

    @PostMapping("/create")
    public String create(@RequestParam("vendorId") @NotNull Long vendorId,
                         @RequestParam("name") @NotBlank @Size(max = 150) String name,
                         @RequestParam(value = "slug", required = false) String slug,
                         RedirectAttributes ra,
                         Model model) {
        try {
            service.create(vendorId, name, slug);
            ra.addFlashAttribute("success", "Tạo kỹ năng thành công.");
            return "redirect:/admin/vendor-skills";
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("skill", new VendorSkill());
            model.addAttribute("vendors", vendorProfiles.findAll());
            model.addAttribute("prefill_vendorId", vendorId);
            model.addAttribute("prefill_name", name);
            model.addAttribute("prefill_slug", slug);
            return "admin/vendor_skills/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, Model model) {
        VendorSkill skill = service.get(id);
        model.addAttribute("skill", skill);
        model.addAttribute("vendors", vendorProfiles.findAll());
        return "admin/vendor_skills/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("vendorId") @NotNull Long vendorId,
                         @RequestParam("name") @NotBlank @Size(max = 150) String name,
                         @RequestParam(value = "slug", required = false) String slug,
                         RedirectAttributes ra,
                         Model model) {
        try {
            service.update(id, vendorId, name, slug);
            ra.addFlashAttribute("success", "Cập nhật kỹ năng thành công.");
            return "redirect:/admin/vendor-skills";
        } catch (Exception ex) {
            VendorSkill s = new VendorSkill();
            s.setId(id);
            s.setVendorId(vendorId);
            s.setName(name);
            s.setSlug(slug);
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("skill", s);
            model.addAttribute("vendors", vendorProfiles.findAll());
            return "admin/vendor_skills/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Đã xoá kỹ năng.");
        return "redirect:/admin/vendor-skills";
    }
    @GetMapping("/vendor/{vendorId}")
    public String vendorDetail(@PathVariable("vendorId") Long vendorId,
                               @RequestParam(value = "q", required = false) String q,
                               @RequestParam(value = "page", defaultValue = "0") int page,
                               @RequestParam(value = "size", defaultValue = "10") int size,
                               Model model) {
        var profile = vendorProfiles.findByUser_Id(vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vendor #" + vendorId));
        var skillsPage = service.list(vendorId, q, page, size);

        model.addAttribute("profile", profile);
        model.addAttribute("skillsPage", skillsPage);
        model.addAttribute("q", q);
        model.addAttribute("size", size);
        return "admin/vendor_skills/vendor_detail";
    }
}
