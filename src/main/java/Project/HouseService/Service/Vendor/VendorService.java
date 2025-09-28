// src/main/java/Project/HouseService/Service/Vendor/VendorService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendorService {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    public VendorService(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    public User getProfile(Long selfId) {
        return users.findById(selfId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + selfId));
    }

    public User updateContact(Long selfId, String email, String phone, String avatarUrl) {
        User u = getProfile(selfId);
        if (email != null) u.setEmail(email);
        if (phone != null) u.setPhone(phone);
        if (avatarUrl != null) u.setAvatarUrl(avatarUrl);
        return users.save(u);
    }

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

    public User deactivateSelf(Long selfId) {
        User u = getProfile(selfId);
        u.setActive(false);
        return users.save(u);
    }
}
