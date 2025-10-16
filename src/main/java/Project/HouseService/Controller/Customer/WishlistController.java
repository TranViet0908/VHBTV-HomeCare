package Project.HouseService.Controller.Customer;

import Project.HouseService.Service.Customer.CustomerWishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class WishlistController {

    private final CustomerWishlistService wishlistService;

    public WishlistController(CustomerWishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    // ========== VIEW PAGES (Thymeleaf) ==========
    // /customer/wishlist/service
    @GetMapping("/customer/wishlist/service")
    public String servicePage(Authentication auth, Model model,
                              @RequestParam(defaultValue = "30") int limit,
                              @RequestParam(defaultValue = "0") int offset) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        List<Map<String, Object>> services = wishlistService.listServicePageModels(userId, limit, offset);
        model.addAttribute("wishlistServices", services);
        return "customer/wishlist/service";
    }

    // /customer/wishlist/vendor
    @GetMapping("/customer/wishlist/vendor")
    public String vendorPage(Authentication auth, Model model,
                             @RequestParam(defaultValue = "30") int limit,
                             @RequestParam(defaultValue = "0") int offset) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        List<Map<String, Object>> vendors = wishlistService.listVendorPageModels(userId, limit, offset);
        model.addAttribute("favoriteVendors", vendors);
        return "customer/wishlist/vendor";
    }

    // ========== JSON API (không /api) ==========
    // Xóa 1 mục wishlist dịch vụ theo wishlistId
    @DeleteMapping("/customer/wishlist/{wishlistId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteWishlistItem(Authentication auth,
                                                                  @PathVariable Long wishlistId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        boolean removed = wishlistService.removeServiceByWishlistId(userId, wishlistId);
        Map<String, Object> body = new HashMap<>();
        body.put("changed", removed);
        return ResponseEntity.ok(body);
    }

    // Xóa vendor khỏi favorites
    @DeleteMapping("/customer/favorites/vendor/{vendorId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteFavoriteVendor(Authentication auth,
                                                                    @PathVariable Long vendorId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        boolean removed = wishlistService.removeVendor(userId, vendorId);
        Map<String, Object> body = new HashMap<>();
        body.put("changed", removed);
        return ResponseEntity.ok(body);
    }

    // Thêm/bỏ wishlist theo vendorServiceId
    @PostMapping("/customer/wishlist/services/{vendorServiceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addService(Authentication auth,
                                                          @PathVariable Long vendorServiceId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        boolean added = wishlistService.addService(userId, vendorServiceId);
        Map<String, Object> body = new HashMap<>();
        body.put("liked", true);
        body.put("changed", added);
        return ResponseEntity.ok(body);
    }

    @DeleteMapping("/customer/wishlist/services/{vendorServiceId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeService(Authentication auth,
                                                             @PathVariable Long vendorServiceId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        boolean removed = wishlistService.removeService(userId, vendorServiceId);
        Map<String, Object> body = new HashMap<>();
        body.put("liked", false);
        body.put("changed", removed);
        return ResponseEntity.ok(body);
    }

    // Thêm vendor vào favorites
    @PostMapping("/customer/favorites/vendors/{vendorId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addVendor(Authentication auth,
                                                         @PathVariable Long vendorId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        boolean added = wishlistService.addVendor(userId, vendorId);
        Map<String, Object> body = new HashMap<>();
        body.put("liked", true);
        body.put("changed", added);
        return ResponseEntity.ok(body);
    }

    // Trạng thái like cho dịch vụ và vendor
    @GetMapping("/customer/wishlist/services/{vendorServiceId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> serviceStatus(Authentication auth,
                                                             @PathVariable Long vendorServiceId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        Map<String, Object> body = new HashMap<>();
        body.put("liked", wishlistService.isServiceWishlisted(userId, vendorServiceId));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/customer/favorites/vendors/{vendorId}/status")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> vendorStatus(Authentication auth,
                                                            @PathVariable Long vendorId) {
        Long userId = wishlistService.requireUserIdByUsername(auth.getName());
        Map<String, Object> body = new HashMap<>();
        body.put("liked", wishlistService.isVendorWishlisted(userId, vendorId));
        return ResponseEntity.ok(body);
    }
}
