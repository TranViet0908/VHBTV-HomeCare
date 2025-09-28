// src/main/java/Project/HouseService/Controller/Customer/CustomerController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Service.Customer.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/user")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    // Lấy thông tin của chính mình
    @GetMapping
    public User me(@RequestHeader("X-USER-ID") Long selfId) {
        return service.getProfile(selfId);
    }

    // Cập nhật liên hệ + avatar — không DTO, lấy trực tiếp từ query/form
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
