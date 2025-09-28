// src/main/java/Project/HouseService/Service/Admin/ReviewServiceAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Repository.VendorServiceReviewRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceAdminService {

    private final VendorServiceReviewRepository repo;

    public ReviewServiceAdminService(VendorServiceReviewRepository repo) {
        this.repo = repo;
    }

    public Page<VendorServiceReview> list(String kw, Long vendorId, Long vendorServiceId,
                                          Boolean hidden, int page, int size) {
        return repo.search(kw, vendorId, vendorServiceId, hidden, PageRequest.of(page, size));
    }

    public VendorServiceReview get(Long id) { return repo.findById(id).orElse(null); }

    @Transactional
    public void hide(Long id, Long adminId, String reason) {
        var r = repo.findById(id).orElseThrow();
        r.setHidden(true);
        r.setHiddenReason(reason);
        r.setHiddenByAdminId(adminId);
        r.setHiddenAt(java.time.LocalDateTime.now());
        repo.save(r);
    }

    @Transactional
    public void unhide(Long id) {
        var r = repo.findById(id).orElseThrow();
        r.setHidden(false);
        r.setHiddenReason(null);
        r.setHiddenByAdminId(null);
        r.setHiddenAt(null);
        repo.save(r);
    }

    @Transactional
    public void delete(Long id) { repo.deleteById(id); }
}
