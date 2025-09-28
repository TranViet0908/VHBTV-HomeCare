// src/main/java/Project/HouseService/Service/Admin/ServiceAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.Service;
import Project.HouseService.Repository.ServiceRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.hibernate.exception.ConstraintViolationException;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

@org.springframework.stereotype.Service
@Transactional
public class ServiceAdminService {

    private final ServiceRepository services;
    private final VendorServiceRepository vendorServices;

    public ServiceAdminService(ServiceRepository services,
                               VendorServiceRepository vendorServices) {
        this.services = services;
        this.vendorServices = vendorServices;
    }

    public Page<Service> list(String key, Long parentId, String unit,
                              int page, int size, String sort) {
        Sort s = Sort.unsorted();
        if (sort != null && !sort.isBlank()) {
            String[] p = sort.split(",", 2);
            if (p.length == 2) {
                s = Sort.by(Sort.Direction.fromString(p[1]), p[0]);
            }
        }
        Pageable pageable = PageRequest.of(Math.max(0, page), Math.max(1, size), s);
        return services.search(key, parentId, unit, pageable);
    }

    public Service create(Service form) {
        if (form.getName() == null || form.getName().isBlank())
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");

        // slug
        if (form.getSlug() == null || form.getSlug().isBlank()) {
            form.setSlug(slugify(form.getName()));
        } else {
            form.setSlug(slugify(form.getSlug()));
        }
        if (services.existsBySlug(form.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }

        // đảm bảo không set parentId chính nó khi tạo
        form.setId(null);
        return services.save(form);
    }

    public Service update(Long id, Service form) {
        Service cur = services.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dịch vụ"));

        // name
        if (form.getName() == null || form.getName().isBlank())
            throw new IllegalArgumentException("Tên dịch vụ không được để trống");
        cur.setName(form.getName());

        // slug
        String newSlug = form.getSlug();
        if (newSlug == null || newSlug.isBlank()) newSlug = form.getName();
        newSlug = slugify(newSlug);
        if (!newSlug.equals(cur.getSlug()) && services.existsBySlug(newSlug)) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        cur.setSlug(newSlug);

        // unit, description, parentId
        cur.setUnit(form.getUnit());
        cur.setDescription(form.getDescription());
        // không cho tự set cha là chính nó
        if (form.getParentId() != null && form.getParentId().equals(id)) {
            throw new IllegalArgumentException("Không thể chọn chính nó làm nhóm cha");
        }
        cur.setParentId(form.getParentId());

        return services.save(cur);
    }

    public void delete(Long id) {
        // chặn xóa nếu có dịch vụ con
        if (services.existsByParentId(id)) {
            throw new IllegalStateException("Hãy xóa/di chuyển các dịch vụ con trước");
        }
        // xóa và dựa vào ràng buộc FK để chặn nếu VendorService đang tham chiếu
        try {
            services.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // spring wraps hibernate ConstraintViolationException khi dính FK
            throw new IllegalStateException("Đang có gói Vendor tham chiếu. Không thể xóa.");
        } catch (ConstraintViolationException e) {
            throw new IllegalStateException("Đang có gói Vendor tham chiếu. Không thể xóa.");
        }
    }

    private String slugify(String input) {
        String s = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.toLowerCase(Locale.ROOT)
                .replace('đ', 'd')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
        if (s.isBlank()) s = "service";
        return s;
    }
}
