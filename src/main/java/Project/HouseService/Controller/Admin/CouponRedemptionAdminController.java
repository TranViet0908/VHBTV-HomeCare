// src/main/java/Project/HouseService/Controller/Admin/CouponRedemptionAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.CouponRedemptionAdminService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/coupons/redemptions")
public class CouponRedemptionAdminController {

    private final CouponRedemptionAdminService service;

    public CouponRedemptionAdminController(CouponRedemptionAdminService service){ this.service = service; }

    @GetMapping
    public String list(@RequestParam(required = false) Long couponId,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model){
        Page<?> data = service.list(couponId, page, size);
        model.addAttribute("data", data);
        model.addAttribute("couponId", couponId);
        return "admin/coupons/redemptions";
    }
}
