// src/main/java/Project/HouseService/Service/Customer/CustomerService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CustomerService {

    private final UserRepository users;
    private final PasswordEncoder encoder; // đảm bảo có Bean PasswordEncoder

    public CustomerService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    // Lấy hồ sơ của chính mình
    public User getProfile(Long selfId) {
        return users.findById(selfId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + selfId));
    }

    // Cập nhật email/phone/avatar
    public User updateContact(Long selfId, String email, String phone, String avatarUrl) {
        User u = getProfile(selfId);
        if (email != null) u.setEmail(email);
        if (phone != null) u.setPhone(phone);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        return users.save(u);
    }

    // Đổi mật khẩu
    public void changePassword(Long selfId, String oldPassword, String newPassword) {
        User u = getProfile(selfId);
        boolean matches = encoder != null
                ? encoder.matches(oldPassword, u.getPassword())
                : (oldPassword != null && oldPassword.equals(u.getPassword()));
        if (!matches) throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        String encoded = encoder != null ? encoder.encode(newPassword) : newPassword;
        u.setPassword(encoded);
        users.save(u);
    }

    // Tự khóa tài khoản
    public User deactivateSelf(Long selfId) {
        User u = getProfile(selfId);
        u.setActive(false);
        return users.save(u);
    }
}
