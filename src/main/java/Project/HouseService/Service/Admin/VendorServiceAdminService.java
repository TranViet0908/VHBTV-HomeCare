// src/main/java/Project/HouseService/Service/Admin/VendorServiceAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@Transactional
public class VendorServiceAdminService {

    private final VendorServiceRepository repo;

    public VendorServiceAdminService(VendorServiceRepository repo) {
        this.repo = repo;
    }

    public Page<VendorService> list(String kw,
                                    Long vendorId,
                                    Long serviceId,
                                    String status,
                                    BigDecimal minPrice,
                                    BigDecimal maxPrice,
                                    Pageable pageable) {
        // status là String, optional validate theo enum DisplayStatus
        if (status != null && !status.isBlank()) {
            boolean ok = Arrays.stream(VendorService.DisplayStatus.values())
                    .anyMatch(e -> e.name().equalsIgnoreCase(status));
            if (!ok) throw new IllegalArgumentException("Trạng thái không hợp lệ: " + status);
        }
        return repo.search(emptyToNull(kw), vendorId, serviceId, emptyToNull(status), minPrice, maxPrice, pageable);
    }

    public VendorService get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy gói dịch vụ id=" + id));
    }

    public VendorService create(VendorService in) {
        validateUnique(in.getVendorId(), in.getServiceId(), in.getTitle(), null);
        LocalDateTime now = LocalDateTime.now();
        if (in.getCreatedAt() == null) in.setCreatedAt(now);
        in.setUpdatedAt(now);
        return repo.save(in);
    }

    public VendorService update(Long id, VendorService in) {
        VendorService cur = get(id);
        validateUnique(in.getVendorId(), in.getServiceId(), in.getTitle(), id);

        cur.setVendorId(in.getVendorId());
        cur.setServiceId(in.getServiceId());
        cur.setTitle(in.getTitle());
        cur.setDescription(in.getDescription());
        cur.setBasePrice(in.getBasePrice());
        cur.setUnit(in.getUnit());
        cur.setDurationMinutes(in.getDurationMinutes());
        cur.setMinNoticeHours(in.getMinNoticeHours());
        cur.setMaxDailyJobs(in.getMaxDailyJobs());
        cur.setStatus(in.getStatus());
        cur.setCoverUrl(in.getCoverUrl());

        cur.setUpdatedAt(LocalDateTime.now());
        return repo.save(cur);
    }

    public void delete(Long id) { repo.deleteById(id); }

    public VendorService toggleStatus(Long id, String newStatus) {
        // validate theo enum DisplayStatus
        boolean ok = Arrays.stream(VendorService.DisplayStatus.values())
                .anyMatch(e -> e.name().equalsIgnoreCase(newStatus));
        if (!ok) throw new IllegalArgumentException("Trạng thái không hợp lệ: " + newStatus);

        VendorService cur = get(id);
        cur.setStatus(newStatus.toUpperCase());
        cur.setUpdatedAt(LocalDateTime.now());
        return repo.save(cur);
    }

    private void validateUnique(Long vendorId, Long serviceId, String title, Long excludeId) {
        if (vendorId == null || serviceId == null || isBlank(title)) {
            throw new IllegalArgumentException("Thiếu vendorId/serviceId/title");
        }
        boolean exists = repo.existsByVendorIdAndServiceIdAndTitleIgnoreCase(vendorId, serviceId, title);
        if (exists) {
            if (excludeId == null || !get(excludeId).getTitle().equalsIgnoreCase(title)
                    || !get(excludeId).getVendorId().equals(vendorId)
                    || !get(excludeId).getServiceId().equals(serviceId)) {
                throw new IllegalStateException("Đã tồn tại gói cùng vendor, service và title");
            }
        }
    }

    private static String emptyToNull(String s) { return isBlank(s) ? null : s; }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}
