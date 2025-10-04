// SAU: src/main/java/Project/HouseService/Controller/Customer/ServiceVendorsController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Service.Customer.VendorBrowsingService;
import Project.HouseService.Repository.VendorServiceRepository;   // +++
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;                                             // +++

@Controller
@RequestMapping
public class ServiceVendorsController {

    private final VendorBrowsingService browsing;
    private final VendorServiceRepository vendorServiceRepository; // +++

    public ServiceVendorsController(VendorBrowsingService browsing,
                                    VendorServiceRepository vendorServiceRepository) { // +++
        this.browsing = browsing;
        this.vendorServiceRepository = vendorServiceRepository;                        // +++
    }

    @GetMapping("/services/{slug}/vendors")
    public String vendorsByService(@PathVariable String slug,
                                   @RequestParam(required = false) String q,
                                   @RequestParam(required = false) Boolean verified,
                                   @RequestParam(required = false) Long priceMin,
                                   @RequestParam(required = false) Long priceMax,
                                   @RequestParam(required = false) Integer durationMin,
                                   @RequestParam(required = false) Integer durationMax,
                                   @RequestParam(required = false) Integer noticeMax,
                                   @RequestParam(defaultValue = "recommend") String sort,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "12") int size,
                                   HttpServletRequest req,
                                   Model model) {

        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 48));
        VendorBrowsingService.Filters filters = new VendorBrowsingService.Filters(
                emptyToNull(q), verified, priceMin, priceMax, durationMin, durationMax, noticeMax
        );

        Long uid = currentUserId();
        String sessionId = req.getSession(true).getId();

        var result = browsing.findVendorServicesByServiceSlug(
                slug, filters, pageable, sort, uid, sessionId
        );

        // Thu thập id các vendor_service trên trang
        List<Long> vsIds = new ArrayList<>();
        for (Object row : result.page.getContent()) {
            if (row instanceof Map<?,?> m) {
                Object id = m.get("id");
                if (id instanceof Number n) vsIds.add(n.longValue());
            } else {
                try { // fallback nếu phần tử là entity
                    Object idObj = row.getClass().getMethod("getId").invoke(row);
                    if (idObj instanceof Number n) vsIds.add(n.longValue());
                } catch (Exception ignored) {}
            }
        }

        // Đếm số đơn theo vendor_service_id
        Map<Long, Long> ordersCountMap = new HashMap<>();
        if (!vsIds.isEmpty()) {
            for (Map<String, Object> r : vendorServiceRepository.countOrdersByVendorServiceIds(vsIds)) {
                Long k = ((Number) r.get("vsId")).longValue();
                Long v = ((Number) r.get("cnt")).longValue();
                ordersCountMap.put(k, v);
            }
        }

        model.addAttribute("serviceSlug", slug);
        model.addAttribute("serviceName", result.service.getName());
        model.addAttribute("serviceDescription", result.service.getDescription());
        model.addAttribute("totalVendors", result.totalVendors);
        model.addAttribute("vendorServices", result.page.getContent());
        model.addAttribute("page", result.page);
        model.addAttribute("ordersCountMap", ordersCountMap); // +++

        return "customer/services/vendors-have-service";
    }

    @GetMapping("/api/services/{slug}/vendors")
    @ResponseBody
    public Map<String, Object> vendorsByServiceApi(@PathVariable String slug,
                                                   @RequestParam(required = false) String q,
                                                   @RequestParam(required = false) Boolean verified,
                                                   @RequestParam(required = false) Long priceMin,
                                                   @RequestParam(required = false) Long priceMax,
                                                   @RequestParam(required = false) Integer durationMin,
                                                   @RequestParam(required = false) Integer durationMax,
                                                   @RequestParam(required = false) Integer noticeMax,
                                                   @RequestParam(defaultValue = "recommend") String sort,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "12") int size,
                                                   HttpServletRequest req) {
        Pageable pageable = PageRequest.of(Math.max(page,0), Math.min(Math.max(size,1), 48));
        VendorBrowsingService.Filters filters = new VendorBrowsingService.Filters(
                emptyToNull(q), verified, priceMin, priceMax, durationMin, durationMax, noticeMax
        );

        Long uid = currentUserId();
        String sessionId = req.getSession(true).getId();

        var result = browsing.findVendorServicesByServiceSlug(
                slug, filters, pageable, sort, uid, sessionId
        );

        // Thu thập id và build ordersCountMap cho API
        List<Long> vsIds = new ArrayList<>();
        for (Object row : result.page.getContent()) {
            if (row instanceof Map<?,?> m) {
                Object id = m.get("id");
                if (id instanceof Number n) vsIds.add(n.longValue());
            }
        }
        Map<Long, Long> ordersCountMap = new HashMap<>();
        if (!vsIds.isEmpty()) {
            for (Map<String, Object> r : vendorServiceRepository.countOrdersByVendorServiceIds(vsIds)) {
                Long k = ((Number) r.get("vsId")).longValue();
                Long v = ((Number) r.get("cnt")).longValue();
                ordersCountMap.put(k, v);
            }
        }

        Map<String, Object> out = new HashMap<>();
        Map<String, Object> service = new HashMap<>();
        service.put("id", result.service.getId());
        service.put("name", result.service.getName());
        service.put("description", result.service.getDescription());
        out.put("service", service);
        out.put("totalVendors", result.totalVendors);
        out.put("page", result.page.getNumber());
        out.put("size", result.page.getSize());
        out.put("totalItems", result.page.getTotalElements());
        out.put("items", result.page.getContent());
        out.put("ordersCountMap", ordersCountMap); // +++
        return out;
    }

    private static String emptyToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    private static Long currentUserId() {
        try {
            Authentication a = SecurityContextHolder.getContext().getAuthentication();
            if (a == null || !a.isAuthenticated()) return null;
            Object p = a.getPrincipal();
            if (p instanceof User u) return u.getId();
        } catch (Exception ignore) {}
        return null;
    }
}
