// src/main/java/Project/HouseService/Service/Vendor/VendorServiceService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.Service;
import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.ServiceRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@org.springframework.stereotype.Service
public class VendorServiceService {

    private final VendorServiceRepository vendorServiceRepo;
    private final ServiceRepository serviceRepo;
    private final UserRepository userRepo;

    public VendorServiceService(VendorServiceRepository vendorServiceRepo,
                                ServiceRepository serviceRepo,
                                UserRepository userRepo) {
        this.vendorServiceRepo = vendorServiceRepo;
        this.serviceRepo = serviceRepo;
        this.userRepo = userRepo;
    }

    public Long currentVendorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String username = auth.getName();
        return userRepo.findByUsername(username).map(User::getId).orElse(null);
    }

    public List<VendorService> listMyServices(Long vendorId) {
        return vendorServiceRepo.findByVendorIdOrderByUpdatedAtDesc(vendorId);
    }

    public List<Service> listAssignable(Long vendorId) {
        return serviceRepo.findAssignableForVendor(vendorId);
    }

    public VendorService findOwned(Long vendorId, Long id) {
        return vendorServiceRepo.findByIdAndVendorId(id, vendorId).orElse(null);
    }

    @Transactional
    public boolean addFromCatalog(Long vendorId, Long serviceId,
                                  BigDecimal basePrice, String customTitle,
                                  MultipartFile cover) {
        if (vendorServiceRepo.existsByVendorIdAndServiceId(vendorId, serviceId)) return false;

        Service svc = serviceRepo.findById(serviceId).orElseThrow();

        VendorService vs = new VendorService();
        vs.setVendorId(vendorId);
        vs.setServiceId(serviceId);
        vs.setTitle((customTitle != null && !customTitle.isBlank()) ? customTitle.trim() : svc.getName());
        vs.setDescription(null);
        vs.setBasePrice(basePrice != null ? basePrice : BigDecimal.ZERO);
        vs.setUnit(svc.getUnit() != null ? svc.getUnit() : "job");
        vs.setDurationMinutes(60);
        vs.setMinNoticeHours(24);
        vs.setMaxDailyJobs(10);
        vs.setStatus("ACTIVE");

        // đảm bảo có ID để làm thư mục {vendorId}/{id}
        vs = vendorServiceRepo.saveAndFlush(vs);

        String url = storeCover(vendorId, vs.getId(), cover);
        if (url != null) {
            vs.setCoverUrl(url);
            vendorServiceRepo.save(vs);
        }
        return true;
    }

    @Transactional
    public boolean updateFull(Long vendorId, Long id,
                              String title, BigDecimal basePrice, String status,
                              String unit, Integer durationMinutes, Integer minNoticeHours, Integer maxDailyJobs,
                              String description, MultipartFile cover) {
        VendorService vs = vendorServiceRepo.findByIdAndVendorId(id, vendorId).orElse(null);
        if (vs == null) return false;

        if (title != null && !title.isBlank()) vs.setTitle(title.trim());
        if (basePrice != null && basePrice.signum() >= 0) vs.setBasePrice(basePrice);
        if (status != null && !status.isBlank()) vs.setStatus(status.trim());
        if (unit != null && !unit.isBlank()) vs.setUnit(unit.trim());
        if (durationMinutes != null && durationMinutes >= 0) vs.setDurationMinutes(durationMinutes);
        if (minNoticeHours != null && minNoticeHours >= 0) vs.setMinNoticeHours(minNoticeHours);
        if (maxDailyJobs != null && maxDailyJobs >= 0) vs.setMaxDailyJobs(maxDailyJobs);
        if (description != null) vs.setDescription(description.trim());

        if (cover != null && !cover.isEmpty()) {
            String uploadedUrl = storeCover(vendorId, id, cover);
            if (uploadedUrl != null) vs.setCoverUrl(uploadedUrl);
        }

        vendorServiceRepo.save(vs);
        return true;
    }

    @Transactional
    public boolean delete(Long vendorId, Long id) {
        return vendorServiceRepo.deleteByIdAndVendorId(id, vendorId) > 0;
    }

    // lưu vào uploads/vendor-services/{vendorId}/{vendorServiceId}/{filename}
    private String storeCover(Long vendorId, Long vendorServiceId, MultipartFile cover) {
        try {
            if (cover == null || cover.isEmpty() || cover.getSize() == 0) return null;

            String original = cover.getOriginalFilename();
            String ext = "";
            if (original != null && original.lastIndexOf('.') >= 0) {
                ext = original.substring(original.lastIndexOf('.')).toLowerCase();
            } else {
                String ct = cover.getContentType();
                if (ct != null && ct.contains("jpeg")) ext = ".jpg";
                else if (ct != null && ct.contains("png")) ext = ".png";
                else if (ct != null && ct.contains("webp")) ext = ".webp";
            }

            String filename = java.util.UUID.randomUUID().toString().replace("-", "") + ext;

            java.nio.file.Path dir = java.nio.file.Paths.get(
                    "uploads", "vendor-services",
                    String.valueOf(vendorId),
                    String.valueOf(vendorServiceId)
            ).toAbsolutePath();
            java.nio.file.Files.createDirectories(dir);

            java.nio.file.Path path = dir.resolve(filename);
            try (var in = cover.getInputStream()) {
                java.nio.file.Files.copy(in, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            // URL public theo WebConfig (/uploads/**)
            return "/uploads/vendor-services/" + vendorId + "/" + vendorServiceId + "/" + filename;
        } catch (Exception e) {
            System.err.println("[storeCover] error: " + e.getMessage());
            return null;
        }
    }
}
