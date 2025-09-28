// src/main/java/Project/HouseService/Controller/Admin/CouponVendorAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.*;
import Project.HouseService.Repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons/{couponId}/vendors")
public class CouponVendorAdminController {

    private final CouponRepository coupons;
    private final CouponVendorRepository links;
    private final UserRepository users;

    public CouponVendorAdminController(CouponRepository coupons,
                                       CouponVendorRepository links,
                                       UserRepository users) {
        this.coupons = coupons; this.links = links; this.users = users;
    }

    @GetMapping
    public String list(@PathVariable Long couponId, Model model){
        Coupon c = coupons.findById(couponId).orElseThrow();
        model.addAttribute("coupon", c);
        model.addAttribute("items", links.findByCoupon_Id(couponId));
        model.addAttribute("vendors", users.findTop200ByRoleOrderByIdAsc(User.Role.ROLE_VENDOR));
        return "admin/coupons/assign_vendors";
    }

    @PostMapping
    public String add(@PathVariable Long couponId,
                      @RequestParam Long vendorId,
                      RedirectAttributes ra){
        if (!links.existsByCoupon_IdAndVendor_Id(couponId, vendorId)) {
            CouponVendor cv = new CouponVendor();
            cv.setCoupon(coupons.getReferenceById(couponId));
            User v = new User(); v.setId(vendorId);
            cv.setVendor(v);
            links.save(cv);
        }
        ra.addFlashAttribute("msg","Đã gán vendor");
        return "redirect:/admin/coupons/" + couponId + "/vendors";
    }

    @PostMapping("/remove")
    public String remove(@PathVariable Long couponId,
                         @RequestParam Long vendorId,
                         RedirectAttributes ra){
        links.deleteByCoupon_IdAndVendor_Id(couponId, vendorId);
        ra.addFlashAttribute("msg","Đã gỡ vendor");
        return "redirect:/admin/coupons/" + couponId + "/vendors";
    }
}
