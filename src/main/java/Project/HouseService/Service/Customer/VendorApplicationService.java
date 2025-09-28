// src/main/java/Project/HouseService/Service/Customer/VendorApplicationService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Entity.VendorApplication.Status;
import Project.HouseService.Repository.VendorApplicationRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendorApplicationService {

    private final VendorApplicationRepository apps;

    @PersistenceContext
    private EntityManager em;

    public VendorApplicationService(VendorApplicationRepository apps) {
        this.apps = apps;
    }

    public VendorApplication myPending(Long userId) {
        return apps.findByUser_IdAndStatus(userId, Status.PENDING).orElse(null);
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
