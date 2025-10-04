// src/main/java/Project/HouseService/Controller/Customer/VendorServiceDetailController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Service.Customer.VendorServiceDetailService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping
public class VendorServiceDetailController {

    private final VendorServiceDetailService detailService;

    public VendorServiceDetailController(VendorServiceDetailService detailService) {
        this.detailService = detailService;
    }

    // HTML view
    @GetMapping("/vendor-services/{id}")
    public String detailPage(@PathVariable Long id,
                             @RequestParam(required = false) Integer page,
                             @RequestParam(required = false) Integer size,
                             Model model) {
        Map<String, Object> data = detailService.loadDetail(id, page, size);
        model.addAllAttributes(data);
        return "customer/services/detail-vendor-service"; // đúng theo đường dẫn bạn yêu cầu
    }

    // JSON cho Postman
    @GetMapping("/api/customer/vendor-services/{id}")
    @ResponseBody
    public ResponseEntity<?> detailApi(@PathVariable Long id,
                                       @RequestParam(required = false) Integer page,
                                       @RequestParam(required = false) Integer size) {
        try {
            Map<String, Object> data = detailService.loadDetail(id, page, size);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "vendorService", data.get("vendorService"),
                            "service", data.get("service"),
                            "vendor", data.get("vendor"),
                            "mediaList", data.get("mediaList"),
                            "ordersCount", data.get("ordersCount"),
                            "avgRating", data.get("avgRating"),
                            "ratingCount", data.get("ratingCount"),
                            "page", data.get("reviewsPage"),
                            "minScheduleAt", data.get("minScheduleAt")
                    )
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Internal error"));
        }
    }
}
