package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Service.Vendor.VendorProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Objects;

@Controller("vendorVendorProfileController")
@RequestMapping("/vendor/profile")
public class VendorProfileController {

    private final VendorProfileService service;

    public VendorProfileController(VendorProfileService service) {
        this.service = service;
    }

    private User resolveCurrent(Principal principal){
        if (principal == null) return null;
        return service.findByUsername(principal.getName())
                .orElseGet(() -> {
                    try {
                        long uid = Long.parseLong(principal.getName());
                        return service.findUser(uid).orElse(null);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                });
    }

    @GetMapping
    public String viewProfile(@RequestParam(value = "uid", required = false) Long uid,
                              Principal principal,
                              Model model) {

        User me = resolveCurrent(principal);
        if (me == null) return "redirect:/login";

        long targetUserId = (uid != null ? uid : me.getId());

        User targetUser = service.findUser(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        VendorProfile vp = service.ensureProfile(targetUserId);

        boolean editable = Objects.equals(me.getId(), targetUserId);

        model.addAttribute("ratingAvg", service.getAvgRating(targetUserId));
        model.addAttribute("ratingCount", service.getRatingCount(targetUserId));
        model.addAttribute("user", targetUser);
        model.addAttribute("vendorProfile", vp);
        model.addAttribute("avatarUrl", targetUser.getAvatarUrl());
        model.addAttribute("editable", editable);
        model.addAttribute("nav", "profile");
        return "vendor/profile/index";
    }

    @GetMapping("/edit")
    public String editForm(Principal principal, Model model) {
        User me = resolveCurrent(principal);
        if (me == null) return "redirect:/login";

        VendorProfile vp = service.ensureProfile(me.getId());

        model.addAttribute("user", me);
        model.addAttribute("vendorProfile", vp);
        model.addAttribute("avatarUrl", me.getAvatarUrl());
        model.addAttribute("nav", "profile");
        return "vendor/profile/edit";
    }

    @PostMapping("/edit")
    public String doEdit(Principal principal,
                         @RequestParam(required = false) String displayName,
                         @RequestParam(required = false) String legalName,
                         @RequestParam(required = false) String bio,
                         @RequestParam(required = false) Integer yearsExperience,
                         @RequestParam(required = false) String addressLine,
                         @RequestParam String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false, name = "avatar") MultipartFile avatar,
                         RedirectAttributes ra) throws Exception {

        User me = resolveCurrent(principal);
        if (me == null) return "redirect:/login";

        if (!StringUtils.hasText(email)) {
            ra.addFlashAttribute("error", "Email không được để trống");
            return "redirect:/vendor/profile/edit";
        }

        service.updateProfileAndAccount(
                me.getId(),
                displayName, legalName, bio, yearsExperience, addressLine,
                email, phone, avatar
        );

        ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        return "redirect:/vendor/profile";
    }
}
