// src/main/java/Project/HouseService/Controller/Vendor/VendorHomeController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class VendorHomeController {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;

    public VendorHomeController(UserRepository userRepository,
                                VendorProfileRepository vendorProfileRepository) {
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
    }

    // Bơm dữ liệu dùng cho header (avatar, vendorProfile) vào mọi view của controller này
    @ModelAttribute
    public void injectHeaderBasics(Authentication auth, Model model) {
        if (auth == null) return;
        String username = auth.getName();

        User u = userRepository.findByUsername(username).orElse(null);
        if (u != null) {
            model.addAttribute("user", u);
            model.addAttribute("avatarUrl", u.getAvatarUrl()); // header.html dùng biến này
        }

        vendorProfileRepository.findByUser_Username(username)
                .ifPresent(vp -> model.addAttribute("vendorProfile", vp));
    }

    @GetMapping("/vendor")
    public String home(Model model) {
        model.addAttribute("nav", "dashboard"); // để sidebar sáng đúng mục
        return "vendor/dashboard";
    }
}
