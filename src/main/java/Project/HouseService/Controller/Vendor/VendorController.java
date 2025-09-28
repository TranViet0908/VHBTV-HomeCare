// src/main/java/Project/HouseService/Controller/Vendor/VendorController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Service.Vendor.VendorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vendor/user")
public class VendorController {

    private final VendorService service;

    public VendorController(VendorService service) {
        this.service = service;
    }

    // Lấy thông tin của chính mình
    @GetMapping
    public User me(@RequestHeader("X-USER-ID") Long selfId) {
        return service.getProfile(selfId);
    }

    // Cập nhật liên hệ + avatar — không DTO
    @PutMapping
    public User update(@RequestHeader("X-USER-ID") Long selfId,
                       @RequestParam(required = false) String email,
                       @RequestParam(required = false) String phone,
                       @RequestParam(required = false, name = "avatarUrl") String avatarUrl) {
        return service.updateContact(selfId, email, phone, avatarUrl);
    }

    // Đổi mật khẩu — không DTO
    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestHeader("X-USER-ID") Long selfId,
                                               @RequestParam String oldPassword,
                                               @RequestParam String newPassword) {
        service.changePassword(selfId, oldPassword, newPassword);
        return ResponseEntity.noContent().build();
    }

    // Tự khóa
    @PostMapping("/deactivate")
    public User deactivate(@RequestHeader("X-USER-ID") Long selfId) {
        return service.deactivateSelf(selfId);
    }
}
