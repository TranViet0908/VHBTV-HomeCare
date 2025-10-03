// src/main/java/Project/HouseService/Controller/Vendor/ServiceReviewVendorController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Entity.VendorService;
import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Service.Vendor.ServiceReviewVendorService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/vendor/service-reviews")
public class ServiceReviewVendorController {

    private final ServiceReviewVendorService service;

    public ServiceReviewVendorController(ServiceReviewVendorService service) {
        this.service = service;
    }

    @GetMapping
    public String index(Model model,
                        @RequestHeader(value = "X-VENDOR-ID", required = false) Long vendorProfileIdHeader,
                        @RequestHeader(value = "X-USER-ID", required = false) Long userIdHeader,
                        @RequestParam(value = "serviceId", required = false) Long serviceId,
                        @RequestParam(value = "ratingMin", required = false) Integer ratingMin,
                        @RequestParam(value = "ratingMax", required = false) Integer ratingMax,
                        @RequestParam(value = "from", required = false) LocalDate from,
                        @RequestParam(value = "to", required = false) LocalDate to,
                        @RequestParam(value = "keyword", required = false) String keyword,
                        @RequestParam(value = "includeHidden", defaultValue = "false") boolean includeHidden,
                        @RequestParam(value = "page", defaultValue = "0") int page,
                        @RequestParam(value = "size", defaultValue = "10") int size) {

        Long vendorUserId    = service.resolveVendorUserId(vendorProfileIdHeader, userIdHeader);
        Long vendorProfileId = service.resolveVendorProfileId(vendorProfileIdHeader, userIdHeader);

        Page<VendorServiceReview> reviews = service.search(
                vendorUserId, serviceId, ratingMin, ratingMax, from, to, keyword, includeHidden, page, size
        );
        Map<String, Object> summary = service.summary(vendorUserId);
        List<VendorService> services = service.servicesOfVendor(vendorProfileId);

        Map<Long, String> svcNames = services.stream()
                .collect(Collectors.toMap(VendorService::getId, VendorService::getTitle, (a,b)->a, LinkedHashMap::new));

        model.addAttribute("nav", "service-reviews");
        model.addAttribute("reviews", reviews);
        model.addAttribute("summary", summary);
        model.addAttribute("services", services);
        model.addAttribute("svcNames", svcNames);

        model.addAttribute("q_serviceId", serviceId);
        model.addAttribute("q_ratingMin", ratingMin);
        model.addAttribute("q_ratingMax", ratingMax);
        model.addAttribute("q_from", from);
        model.addAttribute("q_to", to);
        model.addAttribute("q_keyword", keyword);
        model.addAttribute("q_includeHidden", includeHidden);

        return "vendor/service_reviews/index";
    }

    @GetMapping("/{vendorServiceId}")
    public String detail(Model model,
                         @RequestHeader(value = "X-VENDOR-ID", required = false) Long vendorProfileIdHeader,
                         @RequestHeader(value = "X-USER-ID", required = false) Long userIdHeader,
                         @PathVariable("vendorServiceId") Long vendorServiceId,
                         @RequestParam(value = "ratingMin", required = false) Integer ratingMin,
                         @RequestParam(value = "ratingMax", required = false) Integer ratingMax,
                         @RequestParam(value = "from", required = false) LocalDate from,
                         @RequestParam(value = "to", required = false) LocalDate to,
                         @RequestParam(value = "keyword", required = false) String keyword,
                         @RequestParam(value = "includeHidden", defaultValue = "false") boolean includeHidden,
                         @RequestParam(value = "page", defaultValue = "0") int page,
                         @RequestParam(value = "size", defaultValue = "10") int size) {

        Long vendorUserId    = service.resolveVendorUserId(vendorProfileIdHeader, userIdHeader);
        Long vendorProfileId = service.resolveVendorProfileId(vendorProfileIdHeader, userIdHeader);

        Optional<VendorService> vsOpt = service.findVendorService(vendorServiceId);
        if (vsOpt.isEmpty() || !vsOpt.get().getVendorId().equals(vendorProfileId)) {
            throw new IllegalArgumentException("VendorService không hợp lệ");
        }

        Page<VendorServiceReview> reviews = service.search(
                vendorUserId, vendorServiceId, ratingMin, ratingMax, from, to, keyword, includeHidden, page, size
        );
        Map<String, Object> summary = service.summary(vendorUserId);

        model.addAttribute("nav", "service-reviews");
        model.addAttribute("reviews", reviews);
        model.addAttribute("summary", summary);
        model.addAttribute("vendorService", vsOpt.get());
        return "vendor/service_reviews/detail";
    }

    @PostMapping("/{reviewId}/hide")
    public String hide(@RequestHeader(value = "X-VENDOR-ID", required = false) Long vendorProfileIdHeader,
                       @RequestHeader(value = "X-USER-ID", required = false) Long userIdHeader,
                       @PathVariable("reviewId") Long reviewId,
                       @RequestParam("value") boolean value,
                       @RequestParam(value = "redirect", defaultValue = "/vendor/service-reviews") String redirect) {

        Long vendorUserId = service.resolveVendorUserId(vendorProfileIdHeader, userIdHeader);
        service.setHidden(vendorUserId, reviewId, value);
        return "redirect:" + redirect;
    }
}
