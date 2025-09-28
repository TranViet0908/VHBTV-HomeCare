// src/main/java/Project/HouseService/Controller/Admin/ServiceOrderAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Service.Admin.PaymentAdminService;
import Project.HouseService.Service.Admin.ServiceOrderAdminService;
import Project.HouseService.Service.Admin.ServiceOrderItemAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// +++ thêm import repo
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import java.util.Collections;

@Controller
@RequestMapping("/admin/service-orders")
public class ServiceOrderAdminController {

    private final ServiceOrderAdminService orders;
    private final ServiceOrderItemAdminService items;
    private final PaymentAdminService paymentSvc;

    // +++ repo cho dropdown
    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;

    public ServiceOrderAdminController(ServiceOrderAdminService orders,
                                       ServiceOrderItemAdminService items,
                                       PaymentAdminService paymentSvc,
                                       UserRepository userRepository,
                                       VendorProfileRepository vendorProfileRepository) {
        this.orders = orders;
        this.items = items;
        this.paymentSvc = paymentSvc;
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String status,
                       @RequestParam(required = false) Long vendorId,
                       @RequestParam(required = false) Long customerId,
                       @RequestParam(required = false) LocalDateTime from,
                       @RequestParam(required = false) LocalDateTime to,
                       @RequestParam(required = false) BigDecimal minTotal,
                       @RequestParam(required = false) BigDecimal maxTotal,
                       @RequestParam(required = false) Boolean hasCoupon,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        Page<ServiceOrder> data = orders.search(
                q, status, vendorId, customerId, from, to, minTotal, maxTotal, hasCoupon, PageRequest.of(page, size)
        );
        model.addAttribute("page", data);
        model.addAttribute("query", q);
        model.addAttribute("status", status);
        model.addAttribute("vendorId", vendorId);
        model.addAttribute("customerId", customerId);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("minTotal", minTotal);
        model.addAttribute("maxTotal", maxTotal);
        model.addAttribute("hasCoupon", hasCoupon);

        // +++ cấp dữ liệu cho dropdown theo tên
        try {
            model.addAttribute("vendors", vendorProfileRepository.findAll());
        } catch (Exception e) {
            model.addAttribute("vendors", Collections.emptyList());
        }
        try {
            model.addAttribute("customers", userRepository.findAll());
        } catch (Exception e) {
            model.addAttribute("customers", Collections.emptyList());
        }

        return "admin/service_orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        var so = orders.getOrThrow(id);
        var itemList = items.list(id);
        var paymentList = paymentSvc.listForOrder(id);
        model.addAttribute("order", so);
        model.addAttribute("items", itemList);
        model.addAttribute("payments", paymentList);
        return "admin/service_orders/detail";
    }

    @PostMapping("/{id}/contact")
    public String updateContact(@PathVariable Long id,
                                @RequestParam String contactName,
                                @RequestParam String contactPhone,
                                @RequestParam String addressLine,
                                @RequestParam(required = false) String notes,
                                RedirectAttributes ra) {
        orders.updateContact(id, contactName, contactPhone, addressLine, notes);
        ra.addFlashAttribute("success", "Cập nhật liên hệ thành công.");
        return "redirect:/admin/service-orders/" + id;
    }

    @PostMapping("/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String value,
                               RedirectAttributes ra) {
        orders.changeStatus(id, value);
        ra.addFlashAttribute("success", "Đã đổi trạng thái đơn.");
        return "redirect:/admin/service-orders/" + id;
    }
}
