// src/main/java/Project/HouseService/Service/Admin/VendorServiceMediaAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.VendorService;
import Project.HouseService.Entity.VendorServiceMedia;
import Project.HouseService.Entity.VendorServiceMedia.MediaType;
import Project.HouseService.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Service
@Transactional
public class VendorServiceMediaAdminService {

    private final VendorServiceMediaRepository mediaRepo;
    private final VendorServiceRepository vsRepo;
    private final VendorProfileRepository vpRepo;
    private final ServiceRepository serviceRepo;

    public VendorServiceMediaAdminService(VendorServiceMediaRepository mediaRepo,
                                          VendorServiceRepository vsRepo,
                                          VendorProfileRepository vpRepo,
                                          ServiceRepository serviceRepo) {
        this.mediaRepo = mediaRepo;
        this.vsRepo = vsRepo;
        this.vpRepo = vpRepo;
        this.serviceRepo = serviceRepo;
    }

    public Page<VendorServiceMedia> page(Long vendorId, Long serviceId, MediaType mediaType,
                                         Boolean isCover, String kw, int page, int size) {
        return mediaRepo.search(vendorId, serviceId, mediaType, isCover,
                (kw == null || kw.isBlank()) ? null : kw.trim(),
                PageRequest.of(page, Math.max(1, Math.min(size, 50))));
    }

    public Map<Long,String> vendorNames() {
        var map = new HashMap<Long,String>();
        vpRepo.findAll().forEach(vp -> {
            String name = vp.getDisplayName() != null ? vp.getDisplayName()
                    : (vp.getLegalName() != null ? vp.getLegalName() : ("User #" + vp.getUser().getId()));
            map.put(vp.getUser().getId(), name); // key = vendorId (user_id)
        });
        return map;
    }

    public Map<Long,String> serviceNames() {
        var map = new HashMap<Long,String>();
        serviceRepo.findAll().forEach(s -> map.put(s.getId(), s.getName()));
        return map;
    }

    public void upload(Long vendorServiceId, MultipartFile[] files) throws IOException {
        VendorService vs = vsRepo.findById(vendorServiceId).orElseThrow();
        Long vendorId = vs.getVendorId();

        Path base = Paths.get("uploads/vendor-services/" + vendorId + "/" + vs.getId());
        Files.createDirectories(base);

        boolean hasCover = mediaRepo.existsByVendorService_IdAndCoverTrue(vendorServiceId);
        int order = mediaRepo.countByVendorService_Id(vendorServiceId);

        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String ct = Optional.ofNullable(f.getContentType()).orElse("");
            boolean isImage = ct.startsWith("image/");
            boolean isVideo = ct.startsWith("video/");
            if (!isImage && !isVideo) continue;

            String ext = guessExt(ct, f.getOriginalFilename());
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path dest = base.resolve(filename);
            Files.copy(f.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            VendorServiceMedia m = new VendorServiceMedia();
            m.setVendorService(vs);
            m.setMediaType(isImage ? MediaType.IMAGE : MediaType.VIDEO);
            m.setUrl("/uploads/vendor-services/" + vendorId + "/" + vs.getId() + "/" + filename);
            m.setSortOrder(order++);
            boolean setCover = !hasCover && isImage;
            m.setCover(setCover);
            mediaRepo.save(m);

            if (setCover) { hasCover = true; vs.setCoverUrl(m.getUrl()); }
        }
    }

    public void setCover(Long mediaId) {
        VendorServiceMedia m = mediaRepo.findById(mediaId).orElseThrow();
        Long vsId = m.getVendorService().getId();
        mediaRepo.clearCover(vsId);
        m.setCover(true);
        mediaRepo.save(m);
        VendorService vs = m.getVendorService();
        vs.setCoverUrl(m.getUrl());
    }

    public void delete(Long mediaId) {
        VendorServiceMedia m = mediaRepo.findById(mediaId).orElseThrow();
        Long vsId = m.getVendorService().getId();
        boolean wasCover = m.isCover();

        try {
            String url = m.getUrl(); // "/uploads/..."
            if (url != null && url.startsWith("/uploads/")) {
                Path p = Paths.get(url.substring(1));
                if (Files.exists(p)) Files.delete(p);
            }
        } catch (Exception ignore) {}

        mediaRepo.delete(m);

        if (wasCover) {
            mediaRepo.findFirstByVendorService_IdOrderBySortOrderAscIdAsc(vsId)
                    .ifPresentOrElse(nxt -> {
                        nxt.setCover(true);
                        mediaRepo.save(nxt);
                        nxt.getVendorService().setCoverUrl(nxt.getUrl());
                    }, () -> {
                        VendorService vs = vsRepo.findById(vsId).orElseThrow();
                        vs.setCoverUrl(null);
                    });
        }
    }

    public void reorder(Long vendorServiceId, List<Long> ids, List<Integer> orders) {
        for (int i = 0; i < ids.size(); i++) {
            var m = mediaRepo.findById(ids.get(i)).orElseThrow();
            if (!m.getVendorService().getId().equals(vendorServiceId)) continue;
            m.setSortOrder(orders.get(i));
            mediaRepo.save(m);
        }
    }

    private static String guessExt(String contentType, String original) {
        if ("image/jpeg".equals(contentType)) return ".jpg";
        if ("image/png".equals(contentType))  return ".png";
        if ("image/webp".equals(contentType)) return ".webp";
        if ("video/mp4".equals(contentType))  return ".mp4";
        if ("video/webm".equals(contentType)) return ".webm";
        if (original != null && original.contains(".")) return original.substring(original.lastIndexOf('.'));
        return "";
    }
    public void addVideo(Long vendorServiceId, String url, String altText, Integer sortOrder) {
        if (url == null || url.isBlank()) throw new IllegalArgumentException("URL video không được rỗng");
        String u = url.trim();
        // kiểm tra sơ bộ định dạng
        String low = u.toLowerCase();
        if (!(low.endsWith(".mp4") || low.endsWith(".webm") || low.startsWith("http://") || low.startsWith("https://"))) {
            throw new IllegalArgumentException("Chỉ chấp nhận .mp4, .webm hoặc URL http(s)");
        }
        var vs = vsRepo.findById(vendorServiceId).orElseThrow();

        var m = new Project.HouseService.Entity.VendorServiceMedia();
        m.setVendorService(vs);
        m.setMediaType(Project.HouseService.Entity.VendorServiceMedia.MediaType.VIDEO);
        m.setUrl(u);
        m.setAltText(altText);
        int nextOrder = (sortOrder != null) ? sortOrder : mediaRepo.countByVendorService_Id(vendorServiceId);
        m.setSortOrder(nextOrder);
        m.setCover(false); // video không tự làm cover
        mediaRepo.save(m);
    }
    public VendorServiceMedia findById(Long id) {
        return mediaRepo.findById(id).orElseThrow();
    }
    public void update(Long id, String url, String altText, Integer sortOrder) {
        var m = mediaRepo.findById(id).orElseThrow();
        if (url != null && !url.isBlank() && m.getMediaType() == VendorServiceMedia.MediaType.VIDEO) {
            String u = url.trim().toLowerCase();
            if (!(u.startsWith("http://") || u.startsWith("https://") || u.endsWith(".mp4") || u.endsWith(".webm")))
                throw new IllegalArgumentException("URL video không hợp lệ");
            m.setUrl(url.trim());
        }
        m.setAltText(altText);
        if (sortOrder != null) m.setSortOrder(sortOrder);
        mediaRepo.save(m);
    }
}
