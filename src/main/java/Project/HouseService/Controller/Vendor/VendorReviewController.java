// src/main/java/Project/HouseService/Controller/Vendor/VendorReviewController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorReview;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Service.Vendor.VendorReviewService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendor/vendor-reviews")
public class VendorReviewController {

    private final VendorReviewService reviewService;
    private final VendorProfileRepository vendorProfileRepo;
    private final UserRepository userRepo;

    public VendorReviewController(VendorReviewService reviewService,
                                  VendorProfileRepository vendorProfileRepo,
                                  UserRepository userRepo) {
        this.reviewService = reviewService;
        this.vendorProfileRepo = vendorProfileRepo;
        this.userRepo = userRepo;
    }

    @GetMapping
    public String index(@RequestHeader(value = "X-USER-ID", required = false) Long selfUserId,
                        @RequestParam(required = false) Integer rating,
                        @RequestParam(required = false) String start,
                        @RequestParam(required = false) String end,
                        @RequestParam(required = false) Boolean hasContent,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "createdAt") String sort,
                        @RequestParam(defaultValue = "DESC") String dir,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Model model) {

        Long vendorUserId = resolveVendorUserId(selfUserId); // dùng user.id của vendor
        if (vendorUserId == null) throw new IllegalStateException("Không xác định được vendor user id.");

        LocalDate startDate = parseDate(start);
        LocalDate endDate = parseDate(end);
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

        var summary = reviewService.getVendorSummary(vendorUserId);
        var dist = reviewService.getRatingDistribution(vendorUserId);

        Page<VendorReview> pageData = reviewService.searchReviews(
                vendorUserId, rating, startDate, endDate, hasContent, q, sort, dir, pageable);

        // Map customerId -> display name
        var customerIds = pageData.getContent().stream()
                .map(VendorReview::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> customerNames = new HashMap<>();
        if (!customerIds.isEmpty()) {
            List<User> users = userRepo.findAllById(customerIds);
            for (User u : users) {
                String name = (u.getUsername() != null && !u.getUsername().isBlank())
                        ? u.getUsername()
                        : (u.getEmail() != null && !u.getEmail().isBlank() ? u.getEmail() : ("ID: " + u.getId()));
                customerNames.put(u.getId(), name);
            }
        }

        model.addAttribute("nav", "vendor-reviews");
        model.addAttribute("vendorId", vendorUserId); // hiển thị đúng user.id
        model.addAttribute("summary", summary);
        model.addAttribute("distribution", dist);
        model.addAttribute("page", pageData);
        model.addAttribute("customerNames", customerNames);

        // giữ trạng thái bộ lọc
        model.addAttribute("rating", rating);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAttribute("hasContent", hasContent);
        model.addAttribute("q", q);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);

        return "vendor/vendor_reviews/index";
    }

    // Trả về user.id của vendor
    private Long resolveVendorUserId(Long selfUserId) {
        if (selfUserId != null) return selfUserId;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String name = auth.getName(); // username hoặc email
            if (name != null && !name.isBlank()) {
                var byUsername = userRepo.findByUsername(name);
                if (byUsername.isPresent()) return byUsername.get().getId();

                var byEmail = userRepo.findByEmail(name);
                if (byEmail.isPresent()) return byEmail.get().getId();
            }
        }
        return null;
    }

    private LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); } catch (Exception e) { return null; }
    }
}
