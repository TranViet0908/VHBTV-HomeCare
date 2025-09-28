// src/main/java/Project/HouseService/Controller/Admin/UserAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.User;
import Project.HouseService.Service.Admin.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/users")
public class UserAdminController {

    private final UserService users;

    @Value("${app.upload.dir}")
    private String uploadRoot; // F:/Git/HouseService/uploads

    public UserAdminController(UserService users) {
        this.users = users;
    }

    @GetMapping
    public String list(@RequestParam(value = "search", required = false) String search,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "20") int size,
                       Model model) {
        var p = users.list(search, page, size);
        model.addAttribute("users", p.getContent());
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("page", p.getNumber());
        model.addAttribute("totalPages", p.getTotalPages());
        model.addAttribute("size", p.getSize());
        return "admin/users/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("user", new User());
        return "admin/users/create";
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String create(@ModelAttribute("user") User form,
                         @RequestParam("passwordConfirm") String passwordConfirm,
                         @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                         Model model) throws IOException {
        if (form.getPassword() == null || !form.getPassword().equals(passwordConfirm)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "admin/users/create";
        }
        if (avatar != null && !avatar.isEmpty()) {
            String url = storeAvatar(avatar);
            form.setAvatarUrl(url);
        }
        users.create(form);
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", users.get(id));
        return "admin/users/edit";
    }

    @PostMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String update(@PathVariable Long id,
                         @ModelAttribute("user") User form,
                         @RequestParam(value = "passwordConfirm", required = false) String passwordConfirm,
                         @RequestParam(value = "avatar", required = false) MultipartFile avatar,
                         Model model) throws IOException {
        // Chỉ kiểm tra khi có nhập mật khẩu mới
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            if (passwordConfirm == null || !form.getPassword().equals(passwordConfirm)) {
                model.addAttribute("error", "Mật khẩu xác nhận không khớp");
                model.addAttribute("user", users.get(id));
                return "admin/users/edit";
            }
        }
        if (avatar != null && !avatar.isEmpty()) {
            String url = storeAvatar(avatar);
            form.setAvatarUrl(url);
        } else {
            // Giữ avatar cũ nếu không upload mới
            var existing = users.get(id);
            form.setAvatarUrl(existing.getAvatarUrl());
        }
        users.update(id, form);
        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        users.delete(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id) {
        var u = users.get(id);
        boolean next = !Boolean.TRUE.equals(u.getActive());
        users.toggleActive(id, next);
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/role")
    public String setRole(@PathVariable Long id, @RequestParam("role") User.Role role) {
        users.setRole(id, role);
        return "redirect:/admin/users";
    }

    // Lưu file và trả URL public /uploads/xxx.jpg
    private String storeAvatar(MultipartFile file) throws IOException {
        String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot);

        String filename = java.util.UUID.randomUUID().toString().replace("-", "") + ext;

        // Lưu đúng thư mục avatars
        java.nio.file.Path root = java.nio.file.Paths.get(uploadRoot, "avatars").toAbsolutePath().normalize();
        java.nio.file.Files.createDirectories(root);
        java.nio.file.Path dest = root.resolve(filename);

        try (var in = file.getInputStream()) {
            java.nio.file.Files.copy(in, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
        // URL public khớp ResourceHandler
        return "/uploads/avatars/" + filename;
    }
}
