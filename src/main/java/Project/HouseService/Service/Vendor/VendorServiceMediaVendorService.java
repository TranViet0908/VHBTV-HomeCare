package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.VendorService;
import Project.HouseService.Entity.VendorServiceMedia;
import Project.HouseService.Entity.VendorServiceMedia.MediaType;
import Project.HouseService.Repository.VendorServiceMediaRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorServiceMediaVendorService {

    private final VendorServiceRepository vendorServiceRepo;
    private final VendorServiceMediaRepository mediaRepo;

    public VendorServiceMediaVendorService(VendorServiceRepository vendorServiceRepo,
                                           VendorServiceMediaRepository mediaRepo) {
        this.vendorServiceRepo = vendorServiceRepo;
        this.mediaRepo = mediaRepo;
    }

    /* ======= Lấy danh sách service của vendor ======= */
    public List<Long> listServiceIdsOf(Long vendorId) {
        List<Long> ids = new ArrayList<>();
        // thử các method repo có thể tồn tại để không đòi sửa Repository
        List<?> list = invokeList("findByVendorIdOrderByUpdatedAtDesc", vendorId);
        if (list == null || list.isEmpty()) list = invokeList("findByVendorIdOrderByIdAsc", vendorId);
        if (list == null || list.isEmpty()) list = invokeList("findByVendorId", vendorId);
        if (list != null) for (Object s : list) ids.add(extractId(s));
        // fallback thêm bản đơn lẻ
        if (ids.isEmpty()) {
            Object one = invokeOne("findFirstByVendorIdOrderByIdAsc", vendorId);
            if (one != null) ids.add(extractId(one));
        }
        return ids.stream().filter(Objects::nonNull).toList();
    }

    public Long chooseServiceId(Long vendorId, Long sid) {
        List<Long> ids = listServiceIdsOf(vendorId);
        if (ids.isEmpty()) throw new IllegalStateException("Bạn chưa có dịch vụ nào để quản lý media");
        if (sid != null && ids.contains(sid)) return sid;
        // ưu tiên service có media
        for (Long id : ids) if (mediaRepo.countByVendorService_Id(id) > 0) return id;
        return ids.get(0);
    }

    public Map<Long, Integer> mediaCountsByService(Collection<Long> serviceIds) {
        Map<Long, Integer> m = new LinkedHashMap<>();
        for (Long id : serviceIds) m.put(id, mediaRepo.countByVendorService_Id(id));
        return m;
    }

    private List<?> invokeList(String method, Long vendorId) {
        try {
            Method m = vendorServiceRepo.getClass().getMethod(method, Long.class);
            Object r = m.invoke(vendorServiceRepo, vendorId);
            if (r instanceof List<?> l) return l;
        } catch (Exception ignore) {}
        return Collections.emptyList();
    }

    private Object invokeOne(String method, Long vendorId) {
        try {
            Method m = vendorServiceRepo.getClass().getMethod(method, Long.class);
            return m.invoke(vendorServiceRepo, vendorId);
        } catch (Exception ignore) {}
        return null;
    }

    private Long extractId(Object svc) {
        try { return (Long) svc.getClass().getMethod("getId").invoke(svc); }
        catch (Exception e) { return null; }
    }

    /* ======= Quyền sở hữu + thư mục ======= */

    public VendorService mustOwnService(Long vendorId, Long serviceId) {
        return vendorServiceRepo.findByIdAndVendorId(serviceId, vendorId)
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại hoặc không thuộc về bạn"));
    }

    public Path ensureDir(Long vendorId, Long serviceId) throws IOException {
        Path root = Paths.get("uploads", "vendor-services", String.valueOf(vendorId), String.valueOf(serviceId));
        if (!Files.exists(root)) Files.createDirectories(root);
        return root;
    }

    public String sanitizeFilename(String original) {
        String clean = original == null ? "file" : original.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
        if (!clean.contains(".")) clean = clean + ".bin";
        return UUID.randomUUID().toString().replace("-", "") + "_" + clean;
    }

    /* ======= Query ======= */

    public List<VendorServiceMedia> listByService(Long vendorId, Long serviceId) {
        mustOwnService(vendorId, serviceId);
        return mediaRepo.findByVendorService_IdOrderBySortOrderAscIdAsc(serviceId);
    }

    public Optional<VendorServiceMedia> findByIdForOwner(Long vendorId, Long serviceId, Long mediaId) {
        mustOwnService(vendorId, serviceId);
        return mediaRepo.findById(mediaId).filter(m -> Objects.equals(m.getVendorService().getId(), serviceId));
    }

    /* ======= Create ======= */

    @Transactional
    public int uploadImages(Long vendorId, Long serviceId, List<MultipartFile> files, String altText) throws IOException {
        VendorService svc = mustOwnService(vendorId, serviceId);
        if (files == null || files.isEmpty()) return 0;

        Path dir = ensureDir(vendorId, serviceId);
        int maxOrder = mediaRepo.countByVendorService_Id(serviceId);

        int created = 0;
        for (MultipartFile f : files) {
            if (f == null || f.isEmpty()) continue;
            String fn = sanitizeFilename(Objects.requireNonNull(f.getOriginalFilename()));
            Path dest = dir.resolve(fn);
            Files.copy(f.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            VendorServiceMedia m = new VendorServiceMedia();
            m.setVendorService(svc);
            m.setMediaType(MediaType.IMAGE);
            m.setUrl("/uploads/vendor-services/" + vendorId + "/" + serviceId + "/" + fn);
            m.setAltText(altText != null ? altText : "");
            m.setSortOrder(++maxOrder);
            m.setCover(false);
            mediaRepo.save(m);
            created++;
        }
        return created;
    }

    @Transactional
    public VendorServiceMedia addVideoByUrl(Long vendorId, Long serviceId, String videoUrl, String altText) {
        VendorService svc = mustOwnService(vendorId, serviceId);
        if (!StringUtils.hasText(videoUrl)) throw new IllegalArgumentException("URL video không hợp lệ");

        int maxOrder = mediaRepo.countByVendorService_Id(serviceId);

        VendorServiceMedia m = new VendorServiceMedia();
        m.setVendorService(svc);
        m.setMediaType(MediaType.VIDEO);
        m.setUrl(videoUrl.trim());
        m.setAltText(altText != null ? altText : "");
        m.setSortOrder(maxOrder + 1);
        m.setCover(false);
        return mediaRepo.save(m);
    }

    @Transactional
    public VendorServiceMedia addVideoByFile(Long vendorId, Long serviceId, MultipartFile file, String altText) throws IOException {
        VendorService svc = mustOwnService(vendorId, serviceId);
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Chưa chọn file video");

        Path dir = ensureDir(vendorId, serviceId);
        int maxOrder = mediaRepo.countByVendorService_Id(serviceId);

        String fn = sanitizeFilename(Objects.requireNonNull(file.getOriginalFilename()));
        Path dest = dir.resolve(fn);
        Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        VendorServiceMedia m = new VendorServiceMedia();
        m.setVendorService(svc);
        m.setMediaType(MediaType.VIDEO);
        m.setUrl("/uploads/vendor-services/" + vendorId + "/" + serviceId + "/" + fn);
        m.setAltText(altText != null ? altText : "");
        m.setSortOrder(maxOrder + 1);
        m.setCover(false);
        return mediaRepo.save(m);
    }

    /* ======= Update ======= */

    @Transactional
    public void setCover(Long vendorId, Long serviceId, Long mediaId) {
        VendorService svc = mustOwnService(vendorId, serviceId);
        VendorServiceMedia m = mediaRepo.findById(mediaId)
                .filter(x -> Objects.equals(x.getVendorService().getId(), serviceId))
                .orElseThrow(() -> new IllegalArgumentException("Media không tồn tại"));

        List<VendorServiceMedia> all = mediaRepo.findByVendorService_IdOrderBySortOrderAscIdAsc(serviceId);
        for (VendorServiceMedia x : all) if (x.isCover()) x.setCover(false);
        m.setCover(true);
        mediaRepo.saveAll(all);

        try {
            var coverUrlField = svc.getClass().getDeclaredField("coverUrl");
            coverUrlField.setAccessible(true);
            coverUrlField.set(svc, m.getUrl());
            vendorServiceRepo.save(svc);
        } catch (NoSuchFieldException | IllegalAccessException ignore) {}
    }

    @Transactional
    public void reorder(Long vendorId, Long serviceId, List<Long> orderedIds) {
        mustOwnService(vendorId, serviceId);
        if (orderedIds == null || orderedIds.isEmpty()) return;

        Map<Long, VendorServiceMedia> map = mediaRepo.findByVendorService_IdOrderBySortOrderAscIdAsc(serviceId)
                .stream().collect(Collectors.toMap(VendorServiceMedia::getId, x -> x));

        int order = 1;
        for (Long id : orderedIds) {
            VendorServiceMedia m = map.get(id);
            if (m != null) m.setSortOrder(order++);
        }
        mediaRepo.saveAll(map.values());
    }

    @Transactional
    public void updateMeta(Long vendorId, Long serviceId, Long mediaId, String altText) {
        VendorServiceMedia m = findByIdForOwner(vendorId, serviceId, mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media không tồn tại"));
        m.setAltText(altText != null ? altText : "");
        mediaRepo.save(m);
    }

    /* ======= Delete ======= */

    @Transactional
    public void delete(Long vendorId, Long serviceId, Long mediaId) {
        VendorServiceMedia m = findByIdForOwner(vendorId, serviceId, mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media không tồn tại"));

        if (m.getUrl() != null && m.getUrl().startsWith("/uploads/")) {
            try {
                Path project = Paths.get("").toAbsolutePath();
                Path file = project.resolve(m.getUrl().substring(1));
                Files.deleteIfExists(file);
            } catch (Exception ignore) {}
        }

        boolean wasCover = m.isCover();
        mediaRepo.delete(m);

        if (wasCover) {
            try {
                VendorService svc = m.getVendorService();
                var coverUrlField = svc.getClass().getDeclaredField("coverUrl");
                coverUrlField.setAccessible(true);
                coverUrlField.set(svc, null);
                vendorServiceRepo.save(svc);
            } catch (Exception ignore) {}
        }
    }
    public LinkedHashMap<Long, String> serviceTitlesOf(Long vendorId) {
        LinkedHashMap<Long, String> map = new LinkedHashMap<>();
        List<?> list = invokeList("findByVendorIdOrderByUpdatedAtDesc", vendorId);
        if (list == null || list.isEmpty()) list = invokeList("findByVendorIdOrderByIdAsc", vendorId);
        if (list == null || list.isEmpty()) list = invokeList("findByVendorId", vendorId);

        if (list != null) {
            for (Object s : list) {
                Long id = extractId(s);
                if (id == null) continue;
                String title = extractTitle(s);
                map.put(id, title != null ? title : ("Dịch vụ #" + id));
            }
        }
        if (map.isEmpty()) {
            Object one = invokeOne("findFirstByVendorIdOrderByIdAsc", vendorId);
            if (one != null) {
                Long id = extractId(one);
                String title = extractTitle(one);
                if (id != null) map.put(id, title != null ? title : ("Dịch vụ #" + id));
            }
        }
        return map;
    }

    private String extractTitle(Object svc) {
        try {
            Object v = svc.getClass().getMethod("getTitle").invoke(svc);
            return v != null ? v.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
