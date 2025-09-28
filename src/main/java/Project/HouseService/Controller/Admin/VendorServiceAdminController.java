// src/main/java/Project/HouseService/Controller/Admin/VendorServiceAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.VendorService;
import Project.HouseService.Service.Admin.VendorServiceAdminService;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.ServiceRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/vendor-services")
public class VendorServiceAdminController {

    private final VendorServiceAdminService service;
    private final VendorProfileRepository vendorProfiles;
    private final ServiceRepository servicesRepo;

    public VendorServiceAdminController(VendorServiceAdminService service,
                                        VendorProfileRepository vendorProfiles,
                                        ServiceRepository servicesRepo) {
        this.service = service;
        this.vendorProfiles = vendorProfiles;
        this.servicesRepo = servicesRepo;
    }

    private void addLookups(Model model) {
        var vendors = vendorProfiles.findAll();
        var services = servicesRepo.findAllByOrderByNameAsc();

        Map<Long,String> vendorNames = vendors.stream().collect(Collectors.toMap(
                v -> v.getUser().getId(),
                v -> {
                    String dn = v.getDisplayName();
                    String ln = v.getLegalName();
                    Long uid = v.getUser().getId();
                    return dn != null && !dn.isBlank() ? dn : (ln != null && !ln.isBlank() ? ln : ("User #" + uid));
                }
        ));
        Map<Long,String> serviceNames = services.stream().collect(Collectors.toMap(
                s -> s.getId(), s -> s.getName()
        ));

        model.addAttribute("vendors", vendors);
        model.addAttribute("services", services);
        model.addAttribute("vendorNames", vendorNames);
        model.addAttribute("serviceNames", serviceNames);
    }

    @GetMapping
    public String list(@RequestParam(value = "kw", required = false) String kw,
                       @RequestParam(value = "vendorId", required = false) Long vendorId,
                       @RequestParam(value = "serviceId", required = false) Long serviceId,
                       @RequestParam(value = "status", required = false) String status,
                       @RequestParam(value = "minPrice", required = false) BigDecimal minPrice,
                       @RequestParam(value = "maxPrice", required = false) BigDecimal maxPrice,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size,
                       Model model) {

        Page<VendorService> data = service.list(kw, vendorId, serviceId, status, minPrice, maxPrice, PageRequest.of(page, size));
        model.addAttribute("page", data);
        model.addAttribute("kw", kw);
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("status", status);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        addLookups(model);
        return "admin/vendor_services/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("form", new VendorService());
        addLookups(model);
        return "admin/vendor_services/create";
    }

    @PostMapping("/create")
    public String create(@ModelAttribute("form") VendorService form,
                         @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                         BindingResult result,
                         RedirectAttributes ra,
                         Model model) {
        try {
            String url = storeCoverIfPresent(coverFile, null);
            if (url != null) form.setCoverUrl(url);
            service.create(form);
            ra.addFlashAttribute("success", "Tạo gói dịch vụ thành công");
            return "redirect:/admin/vendor-services";
        } catch (Exception e) {
            result.reject("error", e.getMessage());
            model.addAttribute("form", form);
            addLookups(model);
            return "admin/vendor_services/create";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("item", service.get(id));
        addLookups(model);
        return "admin/vendor_services/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("form", service.get(id));
        addLookups(model);
        return "admin/vendor_services/edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute("form") VendorService form,
                         @RequestParam(value = "coverFile", required = false) MultipartFile coverFile,
                         BindingResult result,
                         RedirectAttributes ra,
                         Model model) {
        try {
            String url = storeCoverIfPresent(coverFile, form.getCoverUrl());
            if (url != null) form.setCoverUrl(url);
            service.update(id, form);
            ra.addFlashAttribute("success", "Cập nhật gói dịch vụ thành công");
            return "redirect:/admin/vendor-services";
        } catch (Exception e) {
            result.reject("error", e.getMessage());
            model.addAttribute("form", form);
            addLookups(model);
            return "admin/vendor_services/edit";
        }
    }

    @PostMapping("/{id}/status")
    public String toggleStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        service.toggleStatus(id, status);
        ra.addFlashAttribute("success", "Đổi trạng thái thành công");
        return "redirect:/admin/vendor-services";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        service.delete(id);
        ra.addFlashAttribute("success", "Đã xóa gói dịch vụ");
        return "redirect:/admin/vendor-services";
    }

    // --- Helpers ---
    private String storeCoverIfPresent(MultipartFile file, String oldUrl) throws IOException {
        if (file == null || file.isEmpty()) return null;

        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/")) {
            throw new IllegalArgumentException("File ảnh không hợp lệ");
        }

        Path root = Paths.get("uploads", "vendor_cover");
        Files.createDirectories(root);

        String ext = getExt(file.getOriginalFilename());
        String name = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = root.resolve(name);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // Xóa file cũ nếu cần:
        // deleteOldIfLocal(oldUrl);

        return "/uploads/vendor_cover/" + name;
    }

    private static String getExt(String original) {
        if (original == null) return ".jpg";
        int i = original.lastIndexOf('.');
        if (i < 0) return ".jpg";
        String e = original.substring(i).toLowerCase();
        return switch (e) {
            case ".jpg", ".jpeg", ".png", ".webp", ".gif" -> e;
            default -> ".jpg";
        };
    }

    @SuppressWarnings("unused")
    private void deleteOldIfLocal(String oldUrl) {
        try {
            if (oldUrl != null && oldUrl.startsWith("/uploads/")) {
                Path p = Paths.get("uploads").resolve(oldUrl.replaceFirst("^/uploads/", ""));
                Files.deleteIfExists(p);
            }
        } catch (Exception ignored) {}
    }
}
