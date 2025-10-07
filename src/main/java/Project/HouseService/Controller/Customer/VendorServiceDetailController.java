// src/main/java/Project/HouseService/Controller/Customer/VendorServiceDetailController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import Project.HouseService.Service.Customer.VendorServiceDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Controller
@RequestMapping
public class VendorServiceDetailController {

    private final VendorServiceDetailService detailService;
    private final VendorProfileRepository vendorProfileRepository;
    private final VendorServiceRepository vendorServiceRepository;

    public VendorServiceDetailController(VendorServiceDetailService detailService,
                                         VendorProfileRepository vendorProfileRepository,
                                         VendorServiceRepository vendorServiceRepository) {
        this.detailService = detailService;
        this.vendorProfileRepository = vendorProfileRepository;
        this.vendorServiceRepository = vendorServiceRepository;
    }

    // Trang chi tiết theo ID
    @GetMapping("/vendor-services/{id}")
    public String detailPage(@PathVariable Long id,
                             @RequestParam(required = false) Integer page,
                             @RequestParam(required = false) Integer size,
                             Model model) {

        // Nếu gói PAUSED thì chuyển sang trang thông báo tạm dừng
        var vsOpt = vendorServiceRepository.findById(id);
        if (vsOpt.isPresent()) {
            var vs = vsOpt.get();
            Object st = vs.getStatus();
            if (st != null && "PAUSED".equalsIgnoreCase(String.valueOf(st))) {
                String vendorName = "nhà cung cấp";
                VendorProfile vp = vendorProfileRepository.findByUserId(vs.getVendorId());
                if (vp != null) {
                    if (vp.getDisplayName() != null && !vp.getDisplayName().isBlank()) {
                        vendorName = vp.getDisplayName();
                    } else if (vp.getUser() != null && vp.getUser().getUsername() != null) {
                        vendorName = vp.getUser().getUsername();
                    }
                }
                model.addAttribute("serviceTitle", vs.getTitle());
                model.addAttribute("vendorName", vendorName);
                return "customer/services/paused-service";
            }
        }

        // Luồng cũ
        Map<String, Object> data = detailService.loadDetail(id, page, size);
        model.addAllAttributes(data);

        // Bổ sung badge hiển thị xác minh + tên vendor cho header
        Long vendorId = null;
        Object vsObj = data.get("vendorService");
        if (vsObj instanceof VendorService vs) {
            vendorId = vs.getVendorId();
        } else if (data.get("vendorId") instanceof Long vId) {
            vendorId = vId;
        }

        if (vendorId != null) {
            VendorProfile vp = vendorProfileRepository.findByUserId(vendorId);
            if (vp != null) {
                if (vp.getDisplayName() != null && !vp.getDisplayName().isBlank()) {
                    model.addAttribute("vendorDisplayName", vp.getDisplayName());
                }
                if (vp.getVerified() != null) {
                    model.addAttribute("vendorVerified", vp.getVerified());
                }
            }
        }

        return "customer/services/detail-vendor-service";
    }

    // Pretty URL: /vendor-services/{vendorName}/{serviceSlug}
    @GetMapping("/vendor-services/{vendorName}/{serviceSlug}")
    public String detailByVendorNameAndSlug(@PathVariable String vendorName,
                                            @PathVariable String serviceSlug) {
        VendorService vs = vendorServiceRepository
                .findByVendorNameAndServiceSlug(vendorName, serviceSlug)
                .orElseThrow(() -> new NoSuchElementException("Vendor service not found"));
        return "forward:/vendor-services/" + vs.getId();
    }

    private Optional<Long> resolveVendorUserId(String key) {
        // 1) username (không phân biệt hoa thường)
        Optional<VendorProfile> byUser = vendorProfileRepository.findByUser_UsernameIgnoreCase(key);
        if (byUser.isPresent()) return Optional.ofNullable(byUser.get().getUser().getId());

        // 2) display_name == key
        Optional<VendorProfile> byDisplay = vendorProfileRepository.findByDisplayNameIgnoreCase(key);
        if (byDisplay.isPresent()) return Optional.ofNullable(byDisplay.get().getUser().getId());

        // 3) slug(display_name) == key
        Optional<VendorProfile> bySlug = vendorProfileRepository.findByDisplayNameSlugIgnoreCase(key);
        return bySlug.map(vp -> vp.getUser().getId());
    }

    // JSON
    @GetMapping("/api/customer/vendor-services/{id}")
    @ResponseBody
    public ResponseEntity<?> detailApi(@PathVariable Long id,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        try {
            Map<String, Object> data = detailService.loadDetail(id, page, size);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "vendorService", data.get("vendorService"),
                            "service", data.get("service"),
                            "vendor", data.get("vendor"),
                            "mediaList", data.get("mediaList"),
                            "ordersCount", data.get("ordersCount"),
                            "avgRating", data.get("avgRating"),
                            "ratingCount", data.get("ratingCount"),
                            "page", data.get("reviewsPage"),
                            "minScheduleAt", data.get("minScheduleAt")
                    )
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Internal error"));
        }
    }
}
