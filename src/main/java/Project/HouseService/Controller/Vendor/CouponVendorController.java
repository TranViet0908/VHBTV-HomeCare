package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.CouponUser;
import Project.HouseService.Entity.User;
import Project.HouseService.Service.Vendor.CouponVendorService;
import Project.HouseService.Repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/vendor/coupons")
public class CouponVendorController {

    private final CouponVendorService service;
    private final UserRepository userRepository;

    public CouponVendorController(CouponVendorService service, UserRepository userRepository) {
        this.service = service;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String index(@RequestParam(value = "q", required = false) String q,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size,
                        Model model) {
        Long vendorUserId = service.currentVendorUserId();
        Page<Coupon> coupons = service.listCouponsForVendor(vendorUserId, q, PageRequest.of(page, size));
        var ids = coupons.stream().map(Coupon::getId).collect(java.util.stream.Collectors.toSet());
        model.addAttribute("nav", "coupons");
        model.addAttribute("q", q);
        model.addAttribute("coupons", coupons);
        model.addAttribute("assignedCounts", service.countAssignedUsers(ids));
        model.addAttribute("redeemedCounts", service.countRedemptions(ids));
        return "vendor/coupons/index";
    }

    @GetMapping("/{id}/assign")
    public String assignPage(@PathVariable("id") Long couponId,
                             @RequestParam(value = "kw", required = false) String kw,
                             @RequestParam(value = "page", defaultValue = "0") int page,
                             @RequestParam(value = "size", defaultValue = "10") int size,
                             Model model) {
        Long vendorUserId = service.currentVendorUserId();
        var assigned = service.listAssignedUsers(vendorUserId, couponId);
        var candidates = userRepository.searchCustomers(kw == null ? "" : kw, PageRequest.of(page, size));
        model.addAttribute("nav", "coupons");
        model.addAttribute("couponId", couponId);
        model.addAttribute("kw", kw);
        model.addAttribute("assigned", assigned);
        model.addAttribute("candidates", candidates);
        return "vendor/coupons/assign";
    }

    @PostMapping("/{id}/assign")
    public String assignSubmit(@PathVariable("id") Long couponId,
                               @RequestParam(value = "userIds", required = false) java.util.List<Long> userIds) {
        Long vendorUserId = service.currentVendorUserId();
        service.assignUsers(vendorUserId, couponId, userIds);
        return "redirect:/vendor/coupons/" + couponId + "/assign?assigned=1";
    }

    @PostMapping("/{id}/users/{userId}/delete")
    public String removeAssigned(@PathVariable("id") Long couponId,
                                 @PathVariable("userId") Long userId) {
        Long vendorUserId = service.currentVendorUserId();
        service.removeUser(vendorUserId, couponId, userId);
        return "redirect:/vendor/coupons/" + couponId + "/assign?removed=1";
    }
}