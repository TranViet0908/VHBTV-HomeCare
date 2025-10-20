// src/main/java/Project/HouseService/Service/Customer/VendorApplicationService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.CustomerProfile;
import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Entity.VendorApplication.Status;
import Project.HouseService.Repository.CustomerProfileRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorApplicationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class VendorApplicationService {

    private final VendorApplicationRepository apps;
    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;

    @PersistenceContext
    private EntityManager em;

    public VendorApplicationService(VendorApplicationRepository apps,
                                   UserRepository userRepository,
                                   CustomerProfileRepository customerProfileRepository) {
        this.apps = apps;
        this.userRepository = userRepository;
        this.customerProfileRepository = customerProfileRepository;
    }

    public VendorApplication myPending(Long userId) {
        return apps.findByUser_IdAndStatus(userId, Status.PENDING).orElse(null);
    }

    /**
     * Lấy dữ liệu pre-fill từ User và CustomerProfile cho form vendor application
     * @param userId ID của user hiện tại đã đăng nhập
     * @return Map chứa dữ liệu pre-fill (fullName, email, phone, address), hoặc empty map nếu không có
     */
    public Map<String, String> getPreFillData(Long userId) {
        Map<String, String> data = new HashMap<>();

        if (userId == null) {
            return data; // empty map
        }

        // Query User
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return data; // empty map
        }

        // Lấy thông tin từ User (email, phone)
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            data.put("email", user.getEmail().trim());
        }
        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            data.put("phone", user.getPhone().trim());
        }

        // Query CustomerProfile (optional - có thể chưa có)
        CustomerProfile profile = customerProfileRepository
            .findByUser_Id(userId)
            .orElse(null);

        if (profile != null) {
            // Lấy fullName từ CustomerProfile
            if (profile.getFullName() != null && !profile.getFullName().isBlank()) {
                data.put("fullName", profile.getFullName().trim());
            }
            // Lấy address từ CustomerProfile
            if (profile.getAddressLine() != null && !profile.getAddressLine().isBlank()) {
                data.put("address", profile.getAddressLine().trim());
            }
        }

        return data;
    }

    public VendorApplication apply(Long userId,
                                   String displayName,
                                   String fullName,
                                   String email,
                                   String phone,
                                   String address,
                                   String region,
                                   Integer experienceYears,
                                   String note) {

        if (userId == null) throw new IllegalArgumentException("Thiếu userId");
        if (apps.existsByUser_IdAndStatus(userId, Status.PENDING)) {
            throw new IllegalStateException("Bạn đã có đơn PENDING");
        }

        displayName = nz(displayName);
        fullName    = nz(fullName);
        email       = z(email);
        phone       = z(phone);
        address     = z(address);
        region      = z(region);
        note        = z(note);

        if (displayName.isEmpty()) throw new IllegalArgumentException("Tên hiển thị là bắt buộc");
        if (fullName.isEmpty())    throw new IllegalArgumentException("Họ tên là bắt buộc");
        if (experienceYears == null || experienceYears < 0) experienceYears = 0;

        VendorApplication a = new VendorApplication();
        a.setUser(em.getReference(User.class, userId));
        a.setDisplayName(displayName);
        a.setFullName(fullName);
        a.setEmail(email);
        a.setPhone(phone);
        a.setAddress(address);
        a.setRegion(region);
        a.setExperienceYears(experienceYears);
        a.setNote(note);
        a.setStatus(Status.PENDING);
        return apps.save(a);
    }

    public void cancel(Long userId, Long applicationId) {
        var a = apps.findById(applicationId).orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại"));
        if (!a.getUser().getId().equals(userId)) throw new IllegalArgumentException("Không có quyền");
        if (a.getStatus() != Status.PENDING) throw new IllegalStateException("Chỉ hủy khi còn PENDING");
        apps.delete(a);
    }

    private static String nz(String s) { s = z(s); if (s.isEmpty()) return s; return s; }
    private static String z(String s) { return s == null ? "" : s.trim(); }
}
