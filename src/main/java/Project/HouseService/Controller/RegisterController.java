// src/main/java/Project/HouseService/Controller/Auth/RegisterController.java
package Project.HouseService.Controller;

import Project.HouseService.Entity.CustomerProfile;
import Project.HouseService.Entity.User;
import Project.HouseService.Service.AuthService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Controller
@RequestMapping("/register")
public class RegisterController {

    private final AuthService auth;

    public RegisterController(AuthService auth) {
        this.auth = auth;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("user", new User()); // cho form bind username/email/pass
        return "auth/register";
    }

    // Nhận avatar từ máy + xác nhận mật khẩu ở view. Gọi service để đồng bộ lưu ảnh.
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String submit(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("passwordConfirm") String passwordConfirm,
                         @RequestParam("email") String email,
                         @RequestParam(value = "phone", required = false) String phone,
                         @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                         @RequestParam(value = "fullName", required = false) String fullName,
                         @RequestParam(value = "dob", required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dob,
                         @RequestParam(value = "gender", required = false) CustomerProfile.Gender gender,
                         @RequestParam(value = "addressLine", required = false) String addressLine,
                         Model model) {

        if (password == null || !password.equals(passwordConfirm)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            model.addAttribute("user", new User());
            return "auth/register";
        }

        // Đăng ký customer. AuthService sẽ tự lưu avatar về /uploads/avatars và tạo CustomerProfile.
        auth.registerCustomer(
                username,
                password,
                email,
                phone,
                avatar,
                fullName,
                dob,
                gender,
                addressLine
        );

        return "redirect:/login";
    }
}
