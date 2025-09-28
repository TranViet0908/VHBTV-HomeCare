// src/main/java/Project/HouseService/Service/Admin/AdminAuthService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminAuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminAuthService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // chỉ cho phép tạo admin đầu tiên
    public User registerFirstAdmin(String username, String rawPassword, String email, String phone) {
        if (users.countByRole(User.Role.ROLE_ADMIN) > 0) {
            throw new IllegalStateException("Đăng ký admin đã bị khóa");
        }
        users.findByUsername(username).ifPresent(u -> { throw new DuplicateKeyException("Username đã tồn tại"); });
        if (email != null && users.findByEmail(email).isPresent()) {
            throw new DuplicateKeyException("Email đã tồn tại");
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(encoder.encode(rawPassword));
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(User.Role.ROLE_ADMIN);
        u.setActive(true);
        return users.save(u);
    }
}
