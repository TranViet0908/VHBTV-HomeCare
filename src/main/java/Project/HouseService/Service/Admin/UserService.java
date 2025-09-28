// src/main/java/Project/HouseService/Service/Admin/UserService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repo, PasswordEncoder passwordEncoder) {
        this.repo = repo;
        this.passwordEncoder = passwordEncoder;
    }

    // Giữ nguyên chữ ký cũ (nếu nơi khác đang dùng)
    public Page<User> list(int page, int size) {
        return repo.findAll(PageRequest.of(page, size));
    }

    // Tìm kiếm + phân trang
    public Page<User> list(String q, int page, int size) {
        if (q == null || q.isBlank()) {
            return list(page, size);
        }
        return repo.search(q.trim(), PageRequest.of(page, size));
    }

    public User get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    }

    public User create(User form) {
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        form.setPassword(passwordEncoder.encode(form.getPassword()));

        if (form.getRole() == null) form.setRole(User.Role.ROLE_CUSTOMER);
        if (form.getActive() == null) form.setActive(Boolean.TRUE);

        try {
            return repo.save(form);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username hoặc Email đã tồn tại", e);
        }
    }

    public User update(Long id, User form) {
        User u = get(id);

        if (form.getUsername() != null && !form.getUsername().isBlank()) u.setUsername(form.getUsername().trim());
        if (form.getEmail() != null && !form.getEmail().isBlank()) u.setEmail(form.getEmail().trim());
        u.setPhone(form.getPhone());
        if (form.getAvatarUrl() != null && !form.getAvatarUrl().isBlank()) {
            u.setAvatarUrl(form.getAvatarUrl()); // chỉ set khi có giá trị
        }
        if (form.getRole() != null) u.setRole(form.getRole());
        if (form.getActive() != null) u.setActive(form.getActive());

        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            u.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        return repo.save(u);
    }

    public void delete(Long id) {
        if (repo.existsById(id)) repo.deleteById(id);
    }

    public User setRole(Long id, User.Role role) {
        User u = get(id);
        u.setRole(role);
        return repo.save(u);
    }

    public User toggleActive(Long id, boolean active) {
        User u = get(id);
        u.setActive(active);
        return repo.save(u);
    }

    // Tùy chọn: bản 1 tham số nếu nơi khác đã dùng
    public User toggleActive(Long id) {
        User u = get(id);
        u.setActive(!Boolean.TRUE.equals(u.getActive()));
        return repo.save(u);
    }
}
