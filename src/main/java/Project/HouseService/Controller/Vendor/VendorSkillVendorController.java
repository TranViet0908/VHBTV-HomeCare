package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorSkill;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Service.Vendor.VendorSkillVendorService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/vendor/skills")
public class VendorSkillVendorController {

    private final VendorSkillVendorService service;
    private final UserRepository userRepo;
    private final VendorProfileRepository profileRepo;

    public VendorSkillVendorController(VendorSkillVendorService service,
                                       UserRepository userRepo,
                                       VendorProfileRepository profileRepo) {
        this.service = service;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    // layout data
    private void prepareLayout(Model model, User user) {
        VendorProfile vp = profileRepo.findByUser_Username(user.getUsername()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("vendorProfile", vp);
        model.addAttribute("avatarUrl", user.getAvatarUrl());
        model.addAttribute("nav", "skills");
    }

    private User currentUser(Authentication auth) {
        return userRepo.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại."));
    }

    @GetMapping
    public String index(Authentication auth, Model model) {
        User u = currentUser(auth);
        prepareLayout(model, u);
        List<VendorSkill> skills = service.list(u.getId());
        model.addAttribute("skills", skills);
        return "vendor/skills/index";
    }

    @GetMapping("/create")
    public String createForm(Authentication auth, Model model) {
        User u = currentUser(auth);
        prepareLayout(model, u);
        if (!model.containsAttribute("skill")) {
            VendorSkill s = new VendorSkill();
            s.setVendorId(u.getId());
            model.addAttribute("skill", s);
        }
        return "vendor/skills/create";
    }

    @PostMapping
    public String create(Authentication auth,
                         @RequestParam String name,
                         @RequestParam(required = false) String slug,
                         RedirectAttributes ra) {
        User u = currentUser(auth);
        try {
            service.create(u.getId(), name, slug);
            ra.addFlashAttribute("success", "Thêm kỹ năng thành công.");
            return "redirect:/vendor/skills";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("skill_name", name);
            ra.addFlashAttribute("skill_slug", slug);
            return "redirect:/vendor/skills/create";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Authentication auth, Model model) {
        User u = currentUser(auth);
        prepareLayout(model, u);
        VendorSkill s = service.list(u.getId()).stream()
                .filter(v -> v.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng."));
        model.addAttribute("skill", s);
        return "vendor/skills/edit";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String name,
                         @RequestParam(required = false) String slug,
                         Authentication auth,
                         RedirectAttributes ra) {
        User u = currentUser(auth);
        try {
            service.update(u.getId(), id, name, slug);
            ra.addFlashAttribute("success", "Cập nhật kỹ năng thành công.");
            return "redirect:/vendor/skills";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("skill_name", name);
            ra.addFlashAttribute("skill_slug", slug);
            return "redirect:/vendor/skills/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
        User u = currentUser(auth);
        try {
            service.delete(u.getId(), id);
            ra.addFlashAttribute("success", "Đã xóa kỹ năng.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/skills";
    }
}
