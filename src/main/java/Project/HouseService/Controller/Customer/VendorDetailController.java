// SAU: src/main/java/Project/HouseService/Controller/Customer/VendorDetailController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Service.Customer.VendorDetailService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class VendorDetailController {

    private final VendorDetailService svc;

    public VendorDetailController(VendorDetailService svc) {
        this.svc = svc;
    }

    @GetMapping({"/vendor", "/vendor/"})
    public String vendorRootRedirect() { return "redirect:/"; }

    @GetMapping("/vendor/{name}")
    public String vendorByName(@PathVariable("name") String name,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "8") int size,
                               Model model) {

        var vendorUser = svc.requireVendorByNameOrUsername(name);
        var summary    = svc.buildVendorSummary(vendorUser);

        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));
        // ĐỔI: lấy cả ACTIVE + PAUSED
        Page<VendorService> services = svc.pageVendorServicesIncludingPaused(vendorUser.getId(), pageable);
        List<Coupon> coupons         = svc.listActiveCouponsForVendor(vendorUser.getId(), 5);

        Map<String, Object> stats = svc.computeVendorStats(vendorUser.getId());

        model.addAttribute("vendor", summary);
        model.addAttribute("vendorServices", services.getContent());
        model.addAttribute("vendorReviews", java.util.Collections.emptyList());
        model.addAttribute("activeCoupons", coupons);
        model.addAttribute("page", services);
        model.addAllAttributes(stats);

        return "customer/vendor/vendor-detail";
    }
}
