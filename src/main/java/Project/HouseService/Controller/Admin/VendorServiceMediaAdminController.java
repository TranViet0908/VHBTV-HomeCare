// src/main/java/Project/HouseService/Controller/Admin/VendorServiceMediaAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.VendorServiceMedia.MediaType;
import Project.HouseService.Repository.*;
import Project.HouseService.Service.Admin.VendorServiceMediaAdminService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/vendor-service-media")
public class VendorServiceMediaAdminController {

    private final VendorServiceMediaAdminService service;
    private final VendorServiceRepository vsRepo;
    private final VendorProfileRepository vpRepo;
    private final ServiceRepository sRepo;

    public VendorServiceMediaAdminController(VendorServiceMediaAdminService service,
                                             VendorServiceRepository vsRepo,
                                             VendorProfileRepository vpRepo,
                                             ServiceRepository sRepo) {
        this.service = service;
        this.vsRepo = vsRepo;
        this.vpRepo = vpRepo;
        this.sRepo = sRepo;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Long vendorId,
                       @RequestParam(required = false) Long serviceId,
                       @RequestParam(required = false) MediaType mediaType,
                       @RequestParam(required = false) Boolean isCover,
                       @RequestParam(required = false) String kw,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {

        var p = service.page(vendorId, serviceId, mediaType, isCover, kw, page, size);
        model.addAttribute("page", p);

        // QUAN TRỌNG: cấp đúng datasets cho dropdown
        model.addAttribute("vendorProfiles", vpRepo.findAllWithUser()); // tên dùng trong list.html, upload.html
        model.addAttribute("vendorServices", vsRepo.findAll());         // tên dùng trong list.html, upload.html

        // giữ tham số lọc
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("serviceId", serviceId);
        model.addAttribute("mediaType", mediaType);
        model.addAttribute("isCover", isCover);
        model.addAttribute("kw", kw);
        return "admin/vendor_service_media/list";
    }

    @GetMapping("/upload")
    public String uploadForm(@RequestParam(required = false) Long vsId, Model model) {
        model.addAttribute("vsId", vsId);
        // QUAN TRỌNG: cấp đúng datasets cho dropdown
        model.addAttribute("vendorProfiles", vpRepo.findAllWithUser());
        model.addAttribute("vendorServices", vsRepo.findAll());
        return "admin/vendor_service_media/upload";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam Long vsId,
                         @RequestParam("files") MultipartFile[] files,
                         RedirectAttributes ra) {
        try {
            service.upload(vsId, files);
            ra.addFlashAttribute("success", "Tải lên thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/vendor-service-media?serviceId=" + vsId;
    }

    @PostMapping("/add-video")
    public String addVideo(@RequestParam Long vsId,
                           @RequestParam String url,
                           @RequestParam(required = false) String altText,
                           @RequestParam(required = false) Integer sortOrder,
                           @RequestParam(required = false) String back,
                           RedirectAttributes ra) {
        try {
            service.addVideo(vsId, url, altText, sortOrder);
            ra.addFlashAttribute("success", "Đã thêm video bằng URL");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:" + (back != null ? back : "/admin/vendor-service-media?serviceId=" + vsId);
    }

    @PostMapping("/{id}/cover")
    public String setCover(@PathVariable Long id,
                           @RequestParam(required = false) String back,
                           RedirectAttributes ra) {
        try {
            service.setCover(id);
            ra.addFlashAttribute("success", "Đã đặt ảnh đại diện");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:" + (back != null ? back : "/admin/vendor-service-media");
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(required = false) String back,
                         RedirectAttributes ra) {
        try {
            service.delete(id);
            ra.addFlashAttribute("success", "Đã xóa");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:" + (back != null ? back : "/admin/vendor-service-media");
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        var m = service.findById(id);
        model.addAttribute("m", m);
        return "admin/vendor_service_media/edit";
    }

    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id,
                         @RequestParam(required = false) String url,
                         @RequestParam(required = false) String altText,
                         @RequestParam Integer sortOrder,
                         RedirectAttributes ra) {
        try {
            service.update(id, url, altText, sortOrder);
            ra.addFlashAttribute("success", "Đã cập nhật");
            var vsId = service.findById(id).getVendorService().getId();
            return "redirect:/admin/vendor-service-media?serviceId=" + vsId;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/vendor-service-media/" + id + "/edit";
        }
    }
}
