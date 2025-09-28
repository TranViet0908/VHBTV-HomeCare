// src/main/java/Project/Hous eService/Service/Admin/ReviewVendorAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.VendorReview;
import Project.HouseService.Repository.VendorReviewRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewVendorAdminService {

    private final VendorReviewRepository repo;
    private final VendorRatingRecalcService recalc;

    public ReviewVendorAdminService(VendorReviewRepository repo, VendorRatingRecalcService recalc) {
        this.repo = repo;
        this.recalc = recalc;
    }

    public Page<VendorReview> list(String kw, Long vendorId, Boolean hidden, int page, int size) {
        return repo.search(kw, vendorId, hidden, PageRequest.of(page, size));
    }

    public VendorReview get(Long id) { return repo.findById(id).orElse(null); }

    @Transactional
    public void hide(Long id, Long adminId, String reason) {
        var r = repo.findById(id).orElseThrow();
        r.setHidden(true);
        r.setHiddenReason(reason);
        r.setHiddenByAdminId(adminId);
        r.setHiddenAt(java.time.LocalDateTime.now());
        repo.save(r);
        recalc.recalcForVendor(r.getVendorId());
    }

    @Transactional
    public void unhide(Long id) {
        var r = repo.findById(id).orElseThrow();
        r.setHidden(false);
        r.setHiddenReason(null);
        r.setHiddenByAdminId(null);
        r.setHiddenAt(null);
        repo.save(r);
        recalc.recalcForVendor(r.getVendorId());
    }

    @Transactional
    public void delete(Long id) {
        var r = repo.findById(id).orElse(null);
        if (r != null) {
            Long vendorId = r.getVendorId();
            repo.deleteById(id);
            recalc.recalcForVendor(vendorId);
        }
    }
}
