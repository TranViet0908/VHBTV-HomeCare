package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@Service
public class VendorProfileService {

    private final VendorProfileRepository vendorProfileRepo;
    private final UserRepository userRepo;
    private final VendorReviewRepository reviewRepo;

    @Value("${app.upload.dir:uploads/avatars}")
    private String avatarBaseDir;

    public VendorProfileService(VendorProfileRepository vendorProfileRepo,
                                UserRepository userRepo,
                                VendorReviewRepository reviewRepo) {
        this.vendorProfileRepo = vendorProfileRepo;
        this.userRepo = userRepo;
        this.reviewRepo = reviewRepo;
    }

    public Optional<User> findUser(long userId){ return userRepo.findById(userId); }

    public Optional<User> findByUsername(String username){ return userRepo.findByUsername(username); }

    public Optional<VendorProfile> findProfileByUserId(long userId){
        return Optional.ofNullable(vendorProfileRepo.findByUserId(userId));
    }

    @Transactional
    public VendorProfile ensureProfile(long userId){
        VendorProfile vp = vendorProfileRepo.findByUserId(userId);
        if (vp != null) return vp;

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        vp = new VendorProfile();
        vp.setUser(user);
        vp.setVerified(Boolean.FALSE);
        // nếu entity có 2 trường tổng hợp thì khởi tạo 0
        try { vp.setRatingAvg(new BigDecimal("0")); } catch (Exception ignored) {}
        try { vp.setRatingCount(0); } catch (Exception ignored) {}
        return vendorProfileRepo.save(vp);
    }

    // ====== Lấy đánh giá thật từ repo (đã có sẵn) ======
    public BigDecimal getAvgRating(long userId) {
        Object[] row = reviewRepo.avgAndCountForVendor(userId); // AVG, COUNT
        if (row == null || row.length < 2 || row[0] == null) return BigDecimal.ZERO;
        // AVG do JPA trả về Double
        return BigDecimal.valueOf(((Number) row[0]).doubleValue());
    }

    public long getRatingCount(long userId) {
        Object[] row = reviewRepo.avgAndCountForVendor(userId);
        if (row == null || row.length < 2 || row[1] == null) return 0L;
        return ((Number) row[1]).longValue();
    }
    // ====================================================

    @Transactional
    public void updateProfileAndAccount(long userId,
                                        String displayName,
                                        String legalName,
                                        String bio,
                                        Integer yearsExp,
                                        String addressLine,
                                        String email,
                                        String phone,
                                        MultipartFile avatarFile) throws IOException {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        VendorProfile vp = ensureProfile(userId);

        vp.setDisplayName(emptyToNull(displayName));
        vp.setLegalName(emptyToNull(legalName));
        vp.setBio(emptyToNull(bio));
        vp.setYearsExperience(yearsExp);
        vp.setAddressLine(emptyToNull(addressLine));
        vendorProfileRepo.save(vp);

        user.setEmail(email);
        user.setPhone(emptyToNull(phone));

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String ext = getExtension(avatarFile.getOriginalFilename());
            String filename = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
            File dir = new File(avatarBaseDir);
            if (!dir.exists()) Files.createDirectories(dir.toPath());
            File target = new File(dir, filename);
            avatarFile.transferTo(target);
            user.setAvatarUrl("/uploads/avatars/" + filename);
        }
        userRepo.save(user);
    }

    private static String emptyToNull(String s){
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private static String getExtension(String name){
        if (!StringUtils.hasText(name) || !name.contains(".")) return "";
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
