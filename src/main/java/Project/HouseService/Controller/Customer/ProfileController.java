// src/main/java/Project/HouseService/Controller/Customer/ProfileController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.CustomerProfile;
import Project.HouseService.Entity.User;
import Project.HouseService.Service.Customer.CustomerWishlistService;
import Project.HouseService.Service.Customer.ProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/customer/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final CustomerWishlistService wishlistService; // <-- thêm

    public ProfileController(ProfileService profileService
                            ,CustomerWishlistService wishlistService) {
        this.profileService = profileService;
        this.wishlistService = wishlistService;
    }

    /** Trang tổng quan hồ sơ + 5 đơn gần nhất. */
    @GetMapping
    @Transactional(readOnly = true)
    public String index(Authentication auth, Model model) {
        String username = auth.getName();
        User user = profileService.requireUserByUsername(username);
        CustomerProfile cp = profileService.findProfileByUserId(user.getId());
        if (cp == null) cp = new CustomerProfile();
        Long userId = user.getId();

        // 5 đơn gần nhất
        List<Map<String, Object>> orders = profileService.recentOrderViews(user.getId(), 5);

        // Thống kê
        long totalOrders = profileService.countOrders(user.getId());
        long completedOrders = profileService.countOrdersByStatus(user.getId(), "COMPLETED");
        long wishlistCount = wishlistService.countService(userId) + wishlistService.countVendor(userId);

        Map<String, Long> orderStats = new LinkedHashMap<>();
        orderStats.put("totalOrders", totalOrders);
        orderStats.put("completedOrders", completedOrders);

        // Tính initials cho avatar fallback
        String baseName = (cp.getFullName()!=null && !cp.getFullName().isBlank())
                ? cp.getFullName().trim()
                : (user.getUsername()!=null ? user.getUsername().trim() : "U");
        String initials;
        if (baseName.contains(" ")) {
            String[] parts = baseName.split("\\s+");
            initials = (parts[0].substring(0,1) + parts[parts.length-1].substring(0,1)).toUpperCase();
        } else {
            initials = baseName.substring(0, Math.min(2, baseName.length())).toUpperCase();
        }

        // Chuẩn hóa URL avatar và đảm bảo file tồn tại ở ./uploads/**
        String avatarUrl = profileService.buildAvatarUrl(user);
        ensureAvatarFileAvailable(avatarUrl);

        model.addAttribute("user", user);
        model.addAttribute("profile", cp);
        model.addAttribute("orders", orders);
        model.addAttribute("orderStats", orderStats);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("initials", initials);
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("wishlistServices", wishlistService.listServicePageModels(userId, 10, 0));
        model.addAttribute("favoriteVendors", wishlistService.listVendorPageModels(userId, 10, 0));
        return "customer/profile/index";
    }

    /** Form chỉnh sửa hồ sơ. */
    @GetMapping("/edit")
    @Transactional(readOnly = true)
    public String editForm(Authentication auth, Model model) {
        String username = auth.getName();
        User user = profileService.requireUserByUsername(username);
        CustomerProfile cp = profileService.ensureProfile(user);

        // để preview ảnh trong form nếu cần
        String avatarUrl = profileService.buildAvatarUrl(user);
        model.addAttribute("avatarUrl", avatarUrl);

        model.addAttribute("user", user);
        model.addAttribute("profile", cp);
        return "customer/profile/edit";
    }

    /** Submit chỉnh sửa hồ sơ + upload avatar (tuỳ chọn). */
    @PostMapping("/edit")
    public String editSubmit(Authentication auth,
                             @RequestParam(name = "full_name", required = false) String fullName,
                             @RequestParam(name = "dob", required = false) String dobStr,
                             @RequestParam(name = "gender", required = false) String gender,
                             @RequestParam(name = "address_line", required = false) String addressLine,
                             @RequestParam(name = "email", required = false) String email,
                             @RequestParam(name = "phone", required = false) String phone,
                             @RequestParam(name = "avatar", required = false) MultipartFile avatar,
                             HttpServletRequest request,
                             RedirectAttributes ra) {
        String username = auth.getName();
        User user = profileService.requireUserByUsername(username);
        CustomerProfile cp = profileService.ensureProfile(user);

        // Parse DOB
        LocalDate dob = null;
        if (dobStr != null && !dobStr.trim().isEmpty()) {
            try {
                dob = LocalDate.parse(dobStr.trim());
                if (dob.isAfter(LocalDate.now())) {
                    ra.addFlashAttribute("error", "Ngày sinh không hợp lệ");
                    return "redirect:/customer/profile/edit";
                }
            } catch (DateTimeParseException e) {
                ra.addFlashAttribute("error", "Định dạng ngày sinh không hợp lệ");
                return "redirect:/customer/profile/edit";
            }
        }

        // Upload avatar (chuẩn hóa public URL)
        String savedPath = null;
        if (avatar != null && !avatar.isEmpty()) {
            if (avatar.getSize() > 2 * 1024 * 1024) {
                ra.addFlashAttribute("error", "Ảnh quá lớn (tối đa 2MB)");
                return "redirect:/customer/profile/edit";
            }
            String ctype = avatar.getContentType();
            if (ctype == null || !ctype.toLowerCase().startsWith("image/")) {
                ra.addFlashAttribute("error", "Tệp không phải ảnh hợp lệ");
                return "redirect:/customer/profile/edit";
            }

            String original = org.springframework.util.StringUtils.cleanPath(Objects.requireNonNull(avatar.getOriginalFilename()));
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot);
            if (ext.length() > 10) ext = "";

            String fname = System.currentTimeMillis() + ext;
            Path root = Paths.get("uploads", "avatars", String.valueOf(user.getId()));
            try {
                Files.createDirectories(root);
                Path target = root.resolve(fname);
                Files.copy(avatar.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                // Public URL luôn bắt đầu bằng "/uploads/..."
                savedPath = "/uploads/avatars/" + user.getId() + "/" + fname;
            } catch (IOException e) {
                ra.addFlashAttribute("error", "Lỗi lưu ảnh đại diện");
                return "redirect:/customer/profile/edit";
            }
        }

        try {
            profileService.updateProfile(
                    user, cp, fullName, dob, gender, addressLine, email, phone, savedPath
            );
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/customer/profile/edit";
        }

        ra.addFlashAttribute("success", "Cập nhật hồ sơ thành công");
        return "redirect:/customer/profile";
    }

    /** Form đổi mật khẩu. */
    @GetMapping("/security")
    @Transactional(readOnly = true)
    public String securityForm(Authentication auth, Model model) {
        String username = auth.getName();
        User user = profileService.requireUserByUsername(username);
        model.addAttribute("user", user);
        return "customer/profile/security";
    }

    /** Submit đổi mật khẩu. */
    @PostMapping("/security")
    public String securitySubmit(Authentication auth,
                                 @RequestParam("current_password") String currentPassword,
                                 @RequestParam("new_password") String newPassword,
                                 @RequestParam("confirm_password") String confirmPassword,
                                 RedirectAttributes ra) {
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("error", "Xác nhận mật khẩu không khớp");
            return "redirect:/customer/profile/security";
        }
        String username = auth.getName();
        User user = profileService.requireUserByUsername(username);
        try {
            profileService.changePassword(user, currentPassword, newPassword);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/customer/profile/security";
        }
        ra.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/customer/profile";
    }
    /* -------------------- helpers trong Controller -------------------- */

    /** Trả về URL hiển thị hợp lệ dưới /uploads/** */
    private String normalizeAvatarUrlForView(String raw, Long userId) {
        if (raw == null || raw.isBlank()) return null;
        String p = raw.trim().replace("\\", "/");

        // đã đúng
        if (p.startsWith("/uploads/")) return p;

        // thiếu dấu /
        if (p.startsWith("uploads/")) return "/" + p;

        // path kiểu ".../avatars/xxx.png" -> chuyển về /uploads/avatars/{userId}/xxx.png
        int idx = p.lastIndexOf("/avatars/");
        if (idx >= 0) {
            String file = p.substring(idx + "/avatars/".length());
            if (file.startsWith("/")) file = file.substring(1);
            return "/uploads/avatars/" + (userId != null ? userId : "unknown") + "/" + file;
        }
        if (p.startsWith("avatars/")) {
            return "/uploads/avatars/" + (userId != null ? userId : "unknown") + "/" + p.substring("avatars/".length());
        }
        if (p.startsWith("/avatars/")) {
            return "/uploads" + p;
        }

        // chỉ có tên file
        return "/uploads/avatars/" + (userId != null ? userId : "unknown") + "/" + p.replaceFirst("^/+", "");
    }

    /** Nếu file chưa có ở ./uploads thì cố gắng copy từ ./target/uploads */
    private void ensureAvatarFileAvailable(String normalizedUrl) {
        if (normalizedUrl == null || !normalizedUrl.startsWith("/uploads/")) return;
        String rel = normalizedUrl.substring("/uploads/".length());

        Path dst = Paths.get("uploads").resolve(rel);
        if (Files.exists(dst)) return;

        Path src = Paths.get("target", "uploads").resolve(rel);
        try {
            if (Files.exists(src)) {
                Files.createDirectories(dst.getParent());
                Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ignored) { }
    }
}
