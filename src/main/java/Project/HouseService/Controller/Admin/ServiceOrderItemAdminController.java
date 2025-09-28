// src/main/java/Project/HouseService/Controller/Admin/ServiceOrderItemAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Service.Admin.ServiceOrderItemAdminService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/service-orders/{orderId}/items")
public class ServiceOrderItemAdminController {

    private final ServiceOrderItemAdminService items;

    public ServiceOrderItemAdminController(ServiceOrderItemAdminService items) {
        this.items = items;
    }

    @GetMapping
    public String list(@PathVariable Long orderId, Model model) {
        model.addAttribute("items", items.list(orderId));
        model.addAttribute("orderId", orderId);
        return "admin/service_orders/items";
    }

    @PostMapping
    public String create(@PathVariable Long orderId,
                         @RequestParam Long vendorId,
                         @RequestParam Long serviceId,
                         @RequestParam LocalDateTime scheduledAt,
                         @RequestParam Integer quantity,
                         @RequestParam BigDecimal unitPrice,
                         RedirectAttributes ra) {
        ServiceOrderItem it = items.create(orderId, vendorId, serviceId, scheduledAt, quantity, unitPrice);
        ra.addFlashAttribute("success", "Đã thêm hạng mục #" + it.getId());
        return "redirect:/admin/service-orders/" + orderId;
    }

    @PostMapping("/{itemId}")
    public String update(@PathVariable Long orderId,
                         @PathVariable Long itemId,
                         @RequestParam LocalDateTime scheduledAt,
                         @RequestParam Integer quantity,
                         @RequestParam BigDecimal unitPrice,
                         RedirectAttributes ra) {
        items.update(itemId, scheduledAt, quantity, unitPrice);
        ra.addFlashAttribute("success", "Đã cập nhật hạng mục.");
        return "redirect:/admin/service-orders/" + orderId;
    }

    @PostMapping("/{itemId}/delete")
    public String delete(@PathVariable Long orderId,
                         @PathVariable Long itemId,
                         RedirectAttributes ra) {
        items.delete(itemId);
        ra.addFlashAttribute("success", "Đã xóa hạng mục.");
        return "redirect:/admin/service-orders/" + orderId;
    }
}
