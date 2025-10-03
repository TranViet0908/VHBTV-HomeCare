package Project.HouseService.Controller.Vendor;

import Project.HouseService.Service.Vendor.VendorServiceMediaVendorService;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class VendorServiceMediaVendorController {

    private final VendorServiceMediaVendorService svc;

    public VendorServiceMediaVendorController(VendorServiceMediaVendorService svc,
                                              VendorServiceRepository vendorServiceRepo) {
        this.svc = svc;
    }

    private Long currentUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null)
            throw new IllegalStateException("Chưa đăng nhập");
        Object p = auth.getPrincipal();
        try { return (Long) p.getClass().getMethod("getId").invoke(p); }
        catch (Exception e) { throw new IllegalStateException("Không lấy được userId từ phiên đăng nhập"); }
    }

    /* ===== LIST: /vendor/media?sid={serviceId} ===== */
    @GetMapping("/vendor/media")
    public String list(@RequestParam(value = "sid", required = false) Long sid,
                       Authentication auth, Model model, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);

            var items = svc.listByService(vendorId, serviceId);
            var serviceIds = svc.listServiceIdsOf(vendorId);
            var mediaCounts = svc.mediaCountsByService(serviceIds);
            var serviceTitles = svc.serviceTitlesOf(vendorId);

            // Tạo nhãn hiển thị: "Tên dịch vụ (số media)". Không để null.
            var serviceLabels = new java.util.LinkedHashMap<Long, String>();
            for (Long id : serviceIds) {
                String title = serviceTitles.get(id);
                Integer cnt = mediaCounts.get(id);
                if (title == null || title.isBlank()) title = "Dịch vụ #" + id;
                if (cnt == null) cnt = 0;
                serviceLabels.put(id, title + " (" + cnt + ")");
            }

            model.addAttribute("items", items);
            model.addAttribute("serviceId", serviceId);
            model.addAttribute("serviceIds", serviceIds);
            model.addAttribute("serviceLabels", serviceLabels); // <-- dùng ở view
            model.addAttribute("nav", "media");
            return "vendor/media/list";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/dashboard";
        }
    }

    /* ===== CREATE ===== */
    @GetMapping("/vendor/media/create")
    public String createForm(@RequestParam(value = "sid", required = false) Long sid,
                             Authentication auth, Model model, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            model.addAttribute("serviceId", serviceId);
            model.addAttribute("nav", "media");
            return "vendor/media/create";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/dashboard";
        }
    }

    @PostMapping("/vendor/media/create")
    public String create(@RequestParam(value = "sid", required = false) Long sid,
                         @RequestParam("mediaType") String mediaType,
                         @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                         @RequestParam(value = "videoFile", required = false) MultipartFile videoFile,
                         @RequestParam(value = "videoUrl", required = false) String videoUrl,
                         @RequestParam(value = "altText", required = false) String altText,
                         Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);

            String t = mediaType == null ? "" : mediaType.trim().toUpperCase();
            switch (t) {
                case "IMAGE":
                    if (imageFile == null || imageFile.isEmpty())
                        throw new IllegalArgumentException("Chọn file ảnh");
                    svc.uploadImages(vendorId, serviceId, List.of(imageFile), altText);
                    ra.addFlashAttribute("success", "Đã tạo ảnh");
                    break;
                case "VIDEO":
                    if (videoFile != null && !videoFile.isEmpty()) {
                        svc.addVideoByFile(vendorId, serviceId, videoFile, altText);
                        ra.addFlashAttribute("success", "Đã tạo video từ file");
                    } else if (StringUtils.hasText(videoUrl)) {
                        svc.addVideoByUrl(vendorId, serviceId, videoUrl, altText);
                        ra.addFlashAttribute("success", "Đã tạo video từ URL");
                    } else {
                        throw new IllegalArgumentException("Chọn file video hoặc nhập URL");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Loại media không hợp lệ");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    /* ===== UPLOAD BATCH ===== */
    @GetMapping("/vendor/media/upload")
    public String uploadForm(@RequestParam(value = "sid", required = false) Long sid,
                             Authentication auth, Model model, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            model.addAttribute("serviceId", serviceId);
            model.addAttribute("nav", "media");
            return "vendor/media/upload";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/dashboard";
        }
    }

    @PostMapping("/vendor/media/upload/images")
    public String uploadImages(@RequestParam(value = "sid", required = false) Long sid,
                               @RequestParam("files") List<MultipartFile> files,
                               @RequestParam(value = "altText", required = false) String altText,
                               Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            int created = svc.uploadImages(vendorId, serviceId, files, altText);
            ra.addFlashAttribute("success", "Đã tải " + created + " ảnh");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    @PostMapping("/vendor/media/upload/video-url")
    public String addVideoUrl(@RequestParam(value = "sid", required = false) Long sid,
                              @RequestParam("videoUrl") String url,
                              @RequestParam(value = "altText", required = false) String altText,
                              Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            svc.addVideoByUrl(vendorId, serviceId, url, altText);
            ra.addFlashAttribute("success", "Đã thêm video URL");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    @PostMapping("/vendor/media/upload/video-file")
    public String addVideoFile(@RequestParam(value = "sid", required = false) Long sid,
                               @RequestParam("file") MultipartFile file,
                               @RequestParam(value = "altText", required = false) String altText,
                               Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            svc.addVideoByFile(vendorId, serviceId, file, altText);
            ra.addFlashAttribute("success", "Đã tải video");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    /* ===== EDIT / COVER / REORDER / DELETE ===== */
    @GetMapping("/vendor/media/{mediaId}/edit")
    public String editForm(@PathVariable Long mediaId,
                           @RequestParam(value = "sid", required = false) Long sid,
                           Authentication auth, Model model, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            var m = svc.findByIdForOwner(vendorId, serviceId, mediaId).orElse(null);
            if (m == null) {
                ra.addFlashAttribute("error", "Không tìm thấy media");
                return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
            }
            model.addAttribute("m", m);
            model.addAttribute("serviceId", serviceId);
            model.addAttribute("nav", "media");
            return "vendor/media/edit";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/vendor/dashboard";
        }
    }

    @PostMapping("/vendor/media/{mediaId}/cover")
    public String setCover(@PathVariable Long mediaId,
                           @RequestParam(value = "sid", required = false) Long sid,
                           Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            svc.setCover(vendorId, serviceId, mediaId);
            ra.addFlashAttribute("success", "Đã đặt làm ảnh bìa");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    @PostMapping("/vendor/media/reorder")
    public String reorder(@RequestParam(value = "sid", required = false) Long sid,
                          @RequestParam("orderedIds") List<Long> orderedIds,
                          Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            svc.reorder(vendorId, serviceId, orderedIds);
            ra.addFlashAttribute("success", "Đã cập nhật thứ tự");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    @PostMapping("/vendor/media/{mediaId}/update")
    public String updateMeta(@PathVariable Long mediaId,
                             @RequestParam(value = "sid", required = false) Long sid,
                             @RequestParam(value = "altText", required = false) String altText,
                             Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            svc.updateMeta(vendorId, serviceId, mediaId, altText);
            ra.addFlashAttribute("success", "Đã cập nhật mô tả");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }

    @PostMapping("/vendor/media/{mediaId}/delete")
    public String delete(@PathVariable Long mediaId,
                         @RequestParam(value = "sid", required = false) Long sid,
                         Authentication auth, RedirectAttributes ra) {
        try {
            Long vendorId = currentUserId(auth);
            Long serviceId = svc.chooseServiceId(vendorId, sid);
            svc.delete(vendorId, serviceId, mediaId);
            ra.addFlashAttribute("success", "Đã xóa media");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/vendor/media?sid=" + (sid != null ? sid : "");
    }
}
