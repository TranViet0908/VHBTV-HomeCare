package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorSkill;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorSkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
public class VendorSkillVendorService {

    private final VendorSkillRepository skillRepo;
    private final UserRepository userRepo;
    private final VendorProfileRepository profileRepo;

    public VendorSkillVendorService(VendorSkillRepository skillRepo,
                                    UserRepository userRepo,
                                    VendorProfileRepository profileRepo) {
        this.skillRepo = skillRepo;
        this.userRepo = userRepo;
        this.profileRepo = profileRepo;
    }

    public User requireUser(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User không tồn tại: " + username));
    }

    public VendorProfile requireProfileByUsername(String username) {
        return profileRepo.findByUser_Username(username)
                .orElseThrow(() -> new IllegalStateException("Vendor chưa tạo hồ sơ."));
    }

    public List<VendorSkill> list(Long vendorUserId) {
        // nếu có method order-by-name thì dùng, không thì lấy theo id asc
        try {
            // optional: nếu đã bổ sung findByVendorIdOrderByNameAsc
            var m = VendorSkillRepository.class.getMethod("findByVendorIdOrderByNameAsc", Long.class);
            @SuppressWarnings("unchecked")
            List<VendorSkill> r = (List<VendorSkill>) m.invoke(skillRepo, vendorUserId);
            return r;
        } catch (Exception ignore) {
            // fallback: dùng page unpaged
            return skillRepo.findByVendorId(vendorUserId, org.springframework.data.domain.Pageable.unpaged())
                    .getContent();
        }
    }

    @Transactional
    public VendorSkill create(Long vendorUserId, String name, String slugInput) {
        if (!StringUtils.hasText(name)) throw new IllegalArgumentException("Tên kỹ năng không được để trống.");
        String slug = StringUtils.hasText(slugInput) ? toSlug(slugInput) : toSlug(name);
        if (skillRepo.existsByVendorIdAndSlug(vendorUserId, slug)) {
            throw new IllegalArgumentException("Kỹ năng đã tồn tại: " + slug);
        }
        VendorSkill s = new VendorSkill();
        s.setVendorId(vendorUserId);
        s.setName(name.trim());
        s.setSlug(slug);
        return skillRepo.save(s);
    }

    @Transactional
    public VendorSkill update(Long vendorUserId, Long id, String name, String slugInput) {
        VendorSkill s = skillRepo.findByIdAndVendorId(id, vendorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng thuộc vendor."));
        if (!StringUtils.hasText(name)) throw new IllegalArgumentException("Tên kỹ năng không được để trống.");
        String newSlug = StringUtils.hasText(slugInput) ? toSlug(slugInput) : toSlug(name);
        if (!newSlug.equalsIgnoreCase(s.getSlug())
                && skillRepo.existsByVendorIdAndSlug(vendorUserId, newSlug)) {
            throw new IllegalArgumentException("Slug đã tồn tại: " + newSlug);
        }
        s.setName(name.trim());
        s.setSlug(newSlug);
        return skillRepo.save(s);
    }

    @Transactional
    public void delete(Long vendorUserId, Long id) {
        VendorSkill s = skillRepo.findByIdAndVendorId(id, vendorUserId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng thuộc vendor."));
        skillRepo.delete(s);
    }

    private String toSlug(String input) {
        String s = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        s = s.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\-\\s]", "-")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        return s;
    }
}
