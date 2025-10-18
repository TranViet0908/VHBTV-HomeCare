package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.VendorReview;
import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Service.Customer.CustomerReviewService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer")
public class ReviewController {

    private final CustomerReviewService reviewService;

    public ReviewController(CustomerReviewService reviewService) {
        this.reviewService = reviewService;
    }

    // ====== Vendor review ======
    @PostMapping("/reviews/vendor/{orderId}")
    public String createVendorReview(@PathVariable("orderId") long orderId,
                                     @RequestParam("rating") int rating,
                                     @RequestParam(value = "content", required = false) String content,
                                     Authentication auth,
                                     Model model) {
        long uid = reviewService.requireUserIdByUsername(auth.getName());
        try {
            var res = reviewService.createVendorReview(uid, orderId, rating, content);
            // Điều hướng về trang chi tiết vendor. Tùy site map, có thể là /customer/vendor/{id}
            long vendorId = (long) res.get("vendorId");
            model.addAttribute("message", "Đánh giá vendor thành công");
            return "redirect:/customer/vendor/" + vendorId;
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            // Quay lại lịch sử đơn nếu lỗi phát sinh
            return "redirect:/customer/orders/history";
        }
    }

    // ====== Service review ======
    @PostMapping("/reviews/service/{soItemId}")
    public String createServiceReview(@PathVariable("soItemId") long soItemId,
                                      @RequestParam("rating") int rating,
                                      @RequestParam(value = "content", required = false) String content,
                                      Authentication auth,
                                      Model model) {
        long uid = reviewService.requireUserIdByUsername(auth.getName());
        try {
            var res = reviewService.createServiceReview(uid, soItemId, rating, content);
            long vendorServiceId = (long) res.get("vendorServiceId");
            model.addAttribute("message", "Đánh giá dịch vụ thành công");
            // Điều hướng về trang chi tiết gói dịch vụ
            return "redirect:/customer/vendor-services/" + vendorServiceId;
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
            // Quay lại lịch sử đơn nếu lỗi
            return "redirect:/customer/orders/history";
        }
    }

    // ====== Lists for display (optional for separate pages) ======
    @GetMapping("/vendors/{vendorId}/reviews")
    public String vendorReviews(@PathVariable("vendorId") long vendorId,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size,
                                Model model) {
        Page<VendorReview> reviews = reviewService.listVendorReviews(vendorId, page, size);
        model.addAttribute("reviews", reviews);
        model.addAttribute("vendorId", vendorId);
        return "customer/vendor/reviews"; // tạo sau nếu cần
    }

    @GetMapping("/vendor-services/{vsId}/reviews")
    public String serviceReviews(@PathVariable("vsId") long vendorServiceId,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) {
        Page<VendorServiceReview> reviews = reviewService.listServiceReviews(vendorServiceId, page, size);
        model.addAttribute("reviews", reviews);
        model.addAttribute("vendorServiceId", vendorServiceId);
        return "customer/vendor_service/reviews"; // tạo sau nếu cần
    }
}
