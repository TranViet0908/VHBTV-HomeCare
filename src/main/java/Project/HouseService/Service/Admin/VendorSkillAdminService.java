package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.VendorSkill;
import Project.HouseService.Repository.VendorSkillRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VendorSkillAdminService {

    private final VendorSkillRepository repo;

    public VendorSkillAdminService(VendorSkillRepository repo) {
        this.repo = repo;
    }

    public Page<VendorSkill> list(Long vendorId, String q, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1), Sort.by(Sort.Direction.DESC, "id"));
        return repo.search(vendorId, emptyToNull(q), pageable);
    }

    public VendorSkill get(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kỹ năng #" + id));
    }

    public VendorSkill create(Long vendorId, String name, String slug) {
        VendorSkill v = new VendorSkill();
        v.setVendorId(vendorId);
        v.setName(name);
        v.setSlug(slug == null || slug.isBlank() ? VendorSkill.toSlug(name) : slug);

        ensureUnique(v.getVendorId(), v.getSlug(), null);
        try {
            return repo.save(v);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Kỹ năng bị trùng trong cùng vendor (vendor_id, slug).");
        }
    }

    public VendorSkill update(Long id, Long vendorId, String name, String slug) {
        VendorSkill v = get(id);
        v.setVendorId(vendorId);
        v.setName(name);
        v.setSlug(slug == null || slug.isBlank() ? VendorSkill.toSlug(name) : slug);

        ensureUnique(v.getVendorId(), v.getSlug(), v.getId());
        try {
            return repo.save(v);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Kỹ năng bị trùng trong cùng vendor (vendor_id, slug).");
        }
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    private void ensureUnique(Long vendorId, String slug, Long selfId) {
        boolean exists = repo.existsByVendorIdAndSlug(vendorId, slug);
        if (exists) {
            if (selfId == null) throw new IllegalArgumentException("Trùng (vendor_id, slug).");
            VendorSkill current = get(selfId);
            if (!(vendorId.equals(current.getVendorId()) && slug.equals(current.getSlug()))) {
                throw new IllegalArgumentException("Trùng (vendor_id, slug).");
            }
        }
    }

    private String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
