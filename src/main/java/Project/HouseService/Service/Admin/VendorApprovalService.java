// src/main/java/Project/HouseService/Service/Admin/VendorApprovalService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Entity.VendorApplication.Status;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorApplicationRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendorApprovalService {

    private final VendorApplicationRepository apps;
    private final VendorProfileRepository profiles;
    private final UserRepository users;

    public VendorApprovalService(VendorApplicationRepository apps,
                                 VendorProfileRepository profiles,
                                 UserRepository users) {
        this.apps = apps;
        this.profiles = profiles;
        this.users = users;
    }

    public Page<VendorApplication> list(Status status, int page, int size) {
        var pr = PageRequest.of(page, size);
        return status == null ? apps.findAll(pr) : apps.findAllByStatus(status, pr);
    }

    public VendorApplication get(Long id) {
        return apps.findById(id).orElseThrow(() -> new IllegalArgumentException("Đơn không tồn tại"));
    }

    public VendorApplication approve(Long id) {
        VendorApplication a = get(id);

        final User userRef = a.getUser();
        final Long uid = userRef.getId();

        // 1) cập nhật trạng thái
        if (a.getStatus() != Status.APPROVED) {
            a.setStatus(Status.APPROVED);
            apps.save(a);
        }

        // 2) upsert vendor_profile
        VendorProfile p = profiles.findByUser_Id(uid).orElseGet(() -> {
            VendorProfile np = new VendorProfile();
            np.setUser(userRef);
            return np;
        });
        p.setDisplayName(a.getDisplayName());
        p.setLegalName(a.getFullName());
        p.setBio(a.getNote());
        p.setYearsExperience(a.getExperienceYears() == null ? 0 : a.getExperienceYears());
        p.setAddressLine(a.getAddress() != null && !a.getAddress().isBlank() ? a.getAddress() : a.getRegion());
        p.setVerified(true);
        profiles.save(p);

        // 3) cập nhật role enum -> ROLE_VENDOR (hoặc VENDOR)
        ensureVendorRole(uid);

        return a;
    }

    public VendorApplication reject(Long id) {
        VendorApplication a = get(id);
        if (a.getStatus() != Status.REJECTED) {
            a.setStatus(Status.REJECTED);
            apps.save(a);
        }
        return a;
    }

    public void delete(Long id) {
        apps.deleteById(id);
    }

    // ===== helpers =====
    private void ensureVendorRole(Long userId) {
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User không tồn tại"));
        User.Role target = resolveVendorEnum();
        if (u.getRole() != target) {
            u.setRole(target);
            users.save(u);
        }
    }

    private User.Role resolveVendorEnum() {
        // Hỗ trợ cả "ROLE_VENDOR" và "VENDOR"
        try {
            return Enum.valueOf(User.Role.class, "ROLE_VENDOR");
        } catch (IllegalArgumentException e1) {
            try {
                return Enum.valueOf(User.Role.class, "VENDOR");
            } catch (IllegalArgumentException e2) {
                throw new IllegalStateException("User.Role không có VENDOR/ROLE_VENDOR");
            }
        }
    }
}
