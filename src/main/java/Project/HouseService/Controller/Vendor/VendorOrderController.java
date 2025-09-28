// src/main/java/Project/HouseService/Controller/Vendor/VendorOrderController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Service.Vendor.VendorOrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/vendor/orders")
public class VendorOrderController {

    private final VendorOrderService svc;
    public VendorOrderController(VendorOrderService svc){ this.svc = svc; }

    @GetMapping
    public String list(@RequestParam(required = false) String status,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) LocalDate from,
                       @RequestParam(required = false) LocalDate to,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "50") int size,
                       Authentication auth, Model model) {
        Long vendorId = svc.currentVendorId(auth.getName());
        var data = svc.list(vendorId, status, q, from, to, page, size);
        model.addAttribute("page", data);
        model.addAttribute("status", status);
        model.addAttribute("q", q);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("nav", "orders");
        return "vendor/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication auth, Model model){
        Long vendorId = svc.currentVendorId(auth.getName());
        var order = svc.get(vendorId, id);
        model.addAttribute("order", order);
        model.addAttribute("items", svc.items(order.getId()));
        model.addAttribute("nav", "orders");
        return "vendor/orders/detail";
    }

    @PostMapping("/{id}/action/{action}")
    public String action(@PathVariable Long id,
                         @PathVariable String action,
                         Authentication auth,
                         RedirectAttributes ra){
        Long vendorId = svc.currentVendorId(auth.getName());
        try{
            svc.transition(vendorId, id, action);
            ra.addFlashAttribute("ok","Đã cập nhật trạng thái");
        }catch(Exception e){
            ra.addFlashAttribute("err", e.getMessage());
        }
        return "redirect:/vendor/orders/{id}";
    }
}
