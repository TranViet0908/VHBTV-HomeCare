// src/main/java/Project/HouseService/Service/AuthService.java
package Project.HouseService.Service;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.CustomerProfile;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.CustomerProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final UserRepository users;
    private final CustomerProfileRepository profiles;
    private final PasswordEncoder encoder;

    // Dùng chung cho tất cả luồng upload. Ví dụ: F:/Git/HouseService/uploads
    @Value("${app.upload.dir:${user.dir}/HouseService/uploads}")
    private String uploadRoot;

    // Giữ tương thích nếu dự án có StorageService sẵn (không bắt buộc phải có)
    @Autowired(required = false)
    private StorageService storage;

    public AuthService(UserRepository users,
                       CustomerProfileRepository profiles,
                       PasswordEncoder encoder) {
        this.users = users;
        this.profiles = profiles;
        this.encoder = encoder;
    }

    // === GIỮ NGUYÊN CÁC HÀM KHÁC CỦA BẠN (login, refresh, changePassword, v.v.) ===

    /** Đăng ký customer + lưu avatar về /uploads/avatars và trả URL công khai. */
    public User registerCustomer(String username,
                                 String rawPassword,
                                 String email,
                                 String phone,
                                 MultipartFile avatar,
                                 String fullName,
                                 LocalDate dob,
                                 CustomerProfile.Gender gender,
                                 String addressLine) {

        users.findByUsername(username)
                .ifPresent(u -> { throw new DuplicateKeyException("Username đã tồn tại"); });

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email là bắt buộc");
        }
        users.findByEmail(email)
                .ifPresent(u -> { throw new DuplicateKeyException("Email đã tồn tại"); });

        User u = new User();
        u.setUsername(username.trim());
        u.setPassword(encoder.encode(rawPassword));
        u.setEmail(email.trim());
        u.setPhone(phone);
        u.setRole(User.Role.ROLE_CUSTOMER);
        u.setActive(true);

        if (avatar != null && !avatar.isEmpty()) {
            // Ưu tiên StorageService nếu dự án đã có; nếu không thì lưu nội bộ.
            String url = (storage != null) ? storage.storeAvatar(avatar) : storeAvatar(avatar);
            u.setAvatarUrl(url); // ví dụ: /uploads/avatars/xxx.png
        }

        u = users.save(u);

        CustomerProfile p = new CustomerProfile();
        p.setUser(u);
        p.setFullName(fullName);
        p.setDob(dob);
        p.setGender(gender);
        p.setAddressLine(addressLine);
        profiles.save(p);

        return u;
    }

    /** Cho phép controller khác (Admin/User) dùng chung logic lưu ảnh, đồng bộ 1 chỗ. */
    public String storeAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;

        // Nếu có StorageService sẵn thì ủy quyền để giữ tương thích tối đa.
        if (storage != null) {
            return storage.storeAvatar(file);
        }

        try {
            String original = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String ext = "";
            int dot = original.lastIndexOf('.');
            if (dot >= 0 && dot < original.length() - 1) ext = original.substring(dot);

            String filename = UUID.randomUUID().toString().replace("-", "") + ext;

            // Gốc: app.upload.dir (VD: F:/Git/HouseService/uploads). Thư mục con: avatars
            Path root = Paths.get(uploadRoot, "avatars").toAbsolutePath().normalize();
            Files.createDirectories(root);

            Path dest = root.resolve(filename);
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            // URL public được WebConfig map: /uploads/** -> file:/F:/Git/HouseService/uploads/
            return "/uploads/avatars/" + filename;
        } catch (Exception e) {
            throw new RuntimeException("Lưu avatar thất bại", e);
        }
    }

    // === class phụ thuộc tùy chọn, chỉ để compile nếu dự án đã có dịch vụ này ===
    public interface StorageService {
        String storeAvatar(MultipartFile file);
    }
}
