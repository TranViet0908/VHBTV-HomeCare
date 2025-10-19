// src/main/java/Project/HouseService/Controller/Customer/VendorDetailController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Service.Customer.CustomerReviewService;
import Project.HouseService.Service.Customer.VendorDetailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendors")
public class VendorDetailController {

    private final VendorDetailService svc;
    private final CustomerReviewService reviewSvc;
    private final UserRepository userRepository;                      // +++

    public VendorDetailController(VendorDetailService svc, CustomerReviewService reviewSvc, UserRepository userRepositor) {
        this.svc = svc;
        this.reviewSvc = reviewSvc;
        this.userRepository = userRepositor;
    }

    @GetMapping("/{name}")
    public String vendorByName(@PathVariable("name") String name,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               @RequestParam(name="rvPage", defaultValue="0") int rvPage,
                               @RequestParam(name="rvSize", defaultValue="3") int rvSize,
                               Authentication auth,
                               Model model) {

        var vendorUser = svc.requireVendorByNameOrUsername(name);
        var summary    = svc.buildVendorSummary(vendorUser);

        Pageable pageable = PageRequest.of(Math.max(page,0), Math.max(size,1));
        Page<VendorService> services = svc.pageVendorServicesIncludingPaused(vendorUser.getId(), pageable);
        List<Coupon> coupons         = svc.listActiveCouponsForVendor(vendorUser.getId(), 5);
        Map<String,Object> stats     = svc.computeVendorStats(vendorUser.getId());

        // reviews: dùng 3 tham số (vendorId, page, size)
        var rv = reviewSvc.listVendorReviews(vendorUser.getId(),
                Math.max(rvPage,0), Math.max(rvSize,1));
        model.addAttribute("vendorReviewsPage", rv);
        model.addAttribute("reviewsHasMore", !rv.isLast());

        // map customerId -> username để hiển thị tên
        var ids = rv.getContent().stream()
                .map(Project.HouseService.Entity.VendorReview::getCustomerId)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        model.addAttribute("customerUsernamesById", reviewSvc.mapUsernamesByIds(ids));

        // bật/tắt form đánh giá: chỉ khi từng có đơn COMPLETED với vendor
        boolean canReview = false;
        if (auth != null && auth.isAuthenticated()) {
            long uid = reviewSvc.requireUserIdByUsername(auth.getName());
            canReview = reviewSvc.hasCompletedOrderWithVendor(uid, vendorUser.getId());
        }
        model.addAttribute("canReviewVendor", canReview);

        model.addAttribute("vendor", summary);
        model.addAttribute("vendorServices", services.getContent());
        model.addAttribute("activeCoupons", coupons);
        model.addAttribute("page", services);
        model.addAllAttributes(stats);

        // giữ lại segment đường dẫn để build action form
        model.addAttribute("pathName", name);

        return "customer/vendor/vendor-detail";
    }

    @PostMapping("/{name}/reviews")
    public String createVendorReview(@PathVariable String name,
                                     @RequestParam int rating,
                                     @RequestParam(required=false) String content,
                                     Authentication auth,
                                     RedirectAttributes ra) {
        if (auth == null || !auth.isAuthenticated()) {
            ra.addFlashAttribute("error", "Bạn cần đăng nhập để đánh giá");
            return "redirect:/vendors/" + name + "#vendor-reviews";
        }
        var vendorUser = svc.requireVendorByNameOrUsername(name);
        long customerId = reviewSvc.requireUserIdByUsername(auth.getName());
        if (!reviewSvc.hasCompletedOrderWithVendor(customerId, vendorUser.getId())) {
            ra.addFlashAttribute("error", "Bạn cần hoàn tất ít nhất 1 đơn với nhà cung cấp này trước khi đánh giá");
            return "redirect:/vendors/" + name + "#vendor-reviews";
        }
        reviewSvc.createVendorReviewByVendor(customerId, vendorUser.getId(), rating, content);
        ra.addFlashAttribute("message", "Đã ghi nhận đánh giá");
        return "redirect:/vendors/" + name + "#vendor-reviews";
    }
}
