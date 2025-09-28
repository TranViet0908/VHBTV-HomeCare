// src/main/java/Project/HouseService/Controller/Admin/CouponUserAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.CouponUser;
import Project.HouseService.Repository.CouponRepository;
import Project.HouseService.Repository.CouponUserRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/coupons/{couponId}/users")
public class CouponUserAdminController {

    private final CouponRepository coupons;
    private final UserRepository users;
    private final CouponUserRepository links;

    public CouponUserAdminController(CouponRepository coupons,
                                     UserRepository users,
                                     CouponUserRepository links) {
        this.coupons = coupons; this.users = users; this.links = links;
    }

    @GetMapping
    public String list(@PathVariable Long couponId, Model model){
        Coupon c = coupons.findById(couponId).orElseThrow();
        model.addAttribute("coupon", c);
        model.addAttribute("items", links.findByCoupon_Id(couponId));
        model.addAttribute("customers", users.findTop200ByRoleOrderByIdAsc(User.Role.ROLE_CUSTOMER));
        return "admin/coupons/assign_users";
    }

    @PostMapping
    public String add(@PathVariable Long couponId,
                      @RequestParam Long userId,
                      RedirectAttributes ra){
        if (!links.existsByCoupon_IdAndUser_Id(couponId, userId)) {
            CouponUser cu = new CouponUser();
            cu.setCoupon(coupons.getReferenceById(couponId));
            User u = new User(); u.setId(userId);
            cu.setUser(u);
            links.save(cu);
        }
        ra.addFlashAttribute("msg","Đã gán customer");
        return "redirect:/admin/coupons/" + couponId + "/users";
    }

    @PostMapping("/remove")
    public String remove(@PathVariable Long couponId,
                         @RequestParam Long userId,
                         RedirectAttributes ra){
        links.deleteByCoupon_IdAndUser_Id(couponId, userId);
        ra.addFlashAttribute("msg","Đã gỡ customer");
        return "redirect:/admin/coupons/" + couponId + "/users";
    }
}
