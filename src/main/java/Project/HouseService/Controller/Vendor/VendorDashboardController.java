// src/main/java/Project/HouseService/Controller/Vendor/VendorDashboardController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Service.Vendor.VendorDashboardService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class VendorDashboardController {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final VendorDashboardService dashboardService;

    public VendorDashboardController(UserRepository userRepository,
                                     VendorProfileRepository vendorProfileRepository,
                                     VendorDashboardService dashboardService) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.dashboardService = dashboardService;
    }

    // Header: user, avatarUrl, vendorProfile
    @ModelAttribute
    public void injectHeaderBasics(Authentication auth, Model model) {
        if (auth == null) return;
        String username = auth.getName();

        User u = userRepository.findByUsername(username).orElse(null);
        if (u != null) {
            model.addAttribute("user", u);
            model.addAttribute("avatarUrl", u.getAvatarUrl());
        }

        vendorProfileRepository.findByUser_Username(username)
                .ifPresent(vp -> model.addAttribute("vendorProfile", vp));
    }

    // Dashboard chính
    @GetMapping("/vendor")
    public String dashboard(Authentication auth, Model model) {
        if (auth == null) return "redirect:/login";
        String username = auth.getName();

        User u = userRepository.findByUsername(username).orElse(null);
        if (u == null) return "redirect:/login";

        long vendorUserId = u.getId(); // vendor_id = user.id
        dashboardService.fillDashboardModel(vendorUserId, model);

        model.addAttribute("nav", "dashboard");
        return "vendor/dashboard";
    }

    // Giữ tương thích nếu có link cũ
    @GetMapping("/vendor/home")
    public String legacyHome() {
        return "redirect:/vendor";
    }
}
