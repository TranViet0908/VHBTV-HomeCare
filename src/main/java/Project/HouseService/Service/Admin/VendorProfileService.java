// src/main/java/Project/HouseService/Service/Admin/VendorProfileService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.VendorProfileRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class VendorProfileService {

    private final VendorProfileRepository profiles;

    public VendorProfileService(VendorProfileRepository profiles) {
        this.profiles = profiles;
    }

    public Page<VendorProfile> page(int page, int size) {
        int p = Math.max(0, page);
        int s = Math.min(Math.max(1, size), 100);
        return profiles.findAll(PageRequest.of(p, s));
    }

    public VendorProfile findById(Long id) {
        return profiles.findById(id).orElseThrow();
    }

    public VendorProfile setVerified(Long profileId, boolean value) {
        VendorProfile vp = profiles.findById(profileId).orElseThrow();
        vp.setVerified(value);
        return profiles.save(vp);
    }
    // NEW: phân trang + lọc
    public Page<VendorProfile> pageFiltered(int page, int size, String q, Boolean verified) {
        String kw = StringUtils.hasText(q) ? q.trim() : null;
        return profiles.search(kw, verified, PageRequest.of(Math.max(0, page), Math.min(100, size)));
    }
    public VendorProfile updateEditableFieldsByProfileId(Long id, VendorProfile form) {
        VendorProfile vp = findById(id);
        if (StringUtils.hasText(form.getDisplayName())) vp.setDisplayName(form.getDisplayName().trim());
        if (StringUtils.hasText(form.getLegalName()))   vp.setLegalName(form.getLegalName().trim());
        if (StringUtils.hasText(form.getBio()))         vp.setBio(form.getBio().trim());
        if (form.getYearsExperience() != null && form.getYearsExperience() >= 0)
            vp.setYearsExperience(form.getYearsExperience());
        if (StringUtils.hasText(form.getAddressLine())) vp.setAddressLine(form.getAddressLine().trim());
        // ratingAvg, ratingCount: chỉ đọc
        return profiles.save(vp);
    }
}
