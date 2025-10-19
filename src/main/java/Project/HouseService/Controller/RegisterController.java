// src/main/java/Project/HouseService/Controller/Auth/RegisterController.java
package Project.HouseService.Controller;

import Project.HouseService.Entity.CustomerProfile;
import Project.HouseService.Entity.User;
import Project.HouseService.Service.AuthService;
import org.springframework.dao.DataIntegrityViolationException;
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

    // Hiển thị form
    @GetMapping
    public String form(Model model) {
        // nếu có flashAttr/error thì Thymeleaf tự đọc, còn không thì khởi tạo trống
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "auth/register";
    }

    // Submit form
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

        // 1) Kiểm tra xác nhận mật khẩu
        if (password == null || !password.equals(passwordConfirm)) {
            fillBack(model, username, email, phone, fullName, dob, gender, addressLine);
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "auth/register";
        }

        try {
            // 2) Thực hiện đăng ký
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
        } catch (IllegalArgumentException | IllegalStateException | DataIntegrityViolationException ex) {
            // 3) Bắt lỗi nghiệp vụ/DB và trả về trang đăng ký kèm thông báo
            fillBack(model, username, email, phone, fullName, dob, gender, addressLine);
            String msg = (ex.getMessage() == null || ex.getMessage().isBlank())
                    ? "Đăng ký thất bại. Vui lòng kiểm tra lại thông tin."
                    : ex.getMessage();
            model.addAttribute("error", msg);
            return "auth/register";
        }

        // 4) Thành công -> chuyển sang trang đăng nhập
        return "redirect:/login?success";
    }

    private void fillBack(Model model,
                          String username,
                          String email,
                          String phone,
                          String fullName,
                          LocalDate dob,
                          CustomerProfile.Gender gender,
                          String addressLine) {
        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("phone", phone);
        model.addAttribute("fullName", fullName);
        model.addAttribute("dob", dob);
        model.addAttribute("gender", gender);
        model.addAttribute("addressLine", addressLine);
    }
}
