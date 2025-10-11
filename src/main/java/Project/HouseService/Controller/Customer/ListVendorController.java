// SAU: src/main/java/Project/HouseService/Controller/Customer/ListVendorController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Service.Customer.ListVendorService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping
public class ListVendorController {

    private final ListVendorService service;

    public ListVendorController(ListVendorService service) {
        this.service = service;
    }

    // View: /vendors
    @GetMapping("/vendors")
    public String vendorsPage(@RequestParam(required = false) String q,
                              @RequestParam(required = false) Boolean verified,
                              @RequestParam(required = false) BigDecimal ratingMin,
                              @RequestParam(required = false) Integer yearsMin,
                              @RequestParam(required = false) BigDecimal priceMin,
                              @RequestParam(required = false) BigDecimal priceMax,
                              @RequestParam(required = false) Integer durationMax,
                              @RequestParam(required = false) Integer noticeMax,
                              @RequestParam(defaultValue = "priority") String sort,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "12") int size,
                              HttpServletRequest request,
                              Model model) {

        Page<Map<String, Object>> results = service.searchVendors(q, verified, ratingMin, yearsMin,
                priceMin, priceMax, durationMax, noticeMax, sort, page, size, request);

        model.addAttribute("items", results.getContent());
        model.addAttribute("page", results.getNumber());
        model.addAttribute("size", results.getSize());
        model.addAttribute("total", results.getTotalElements());
        List<Map<String, Object>> suggested = service.suggestedVendors(request, 8);
        model.addAttribute("facets", service.facets());
        model.addAttribute("suggested", suggested);
        model.addAttribute("suggestedIds",
                suggested.stream()
                        .map(m -> ((Number) m.get("vendorId")).longValue())
                        .toList());

        Map<String, Object> params = new HashMap<>();
        params.put("q", q);
        params.put("verified", verified);
        params.put("ratingMin", ratingMin);
        params.put("yearsMin", yearsMin);
        params.put("priceMin", priceMin);
        params.put("priceMax", priceMax);
        params.put("durationMax", durationMax);
        params.put("noticeMax", noticeMax);
        params.put("sort", sort);
        model.addAttribute("params", params);

        return "customer/vendor/vendors";
    }

    // API: /api/vendors
    @GetMapping(value = "/api/vendors", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> vendorsApi(@RequestParam(required = false) String q,
                                          @RequestParam(required = false) Boolean verified,
                                          @RequestParam(required = false) BigDecimal ratingMin,
                                          @RequestParam(required = false) Integer yearsMin,
                                          @RequestParam(required = false) BigDecimal priceMin,
                                          @RequestParam(required = false) BigDecimal priceMax,
                                          @RequestParam(required = false) Integer durationMax,
                                          @RequestParam(required = false) Integer noticeMax,
                                          @RequestParam(defaultValue = "priority") String sort,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "12") int size,
                                          HttpServletRequest request) {

        Page<Map<String, Object>> results = service.searchVendors(q, verified, ratingMin, yearsMin,
                priceMin, priceMax, durationMax, noticeMax, sort, page, size, request);

        Map<String, Object> resp = new HashMap<>();
        resp.put("items", results.getContent());
        resp.put("page", results.getNumber());
        resp.put("size", results.getSize());
        resp.put("total", results.getTotalElements());
        resp.put("facets", service.facets());
        resp.put("suggested", service.suggestedVendors(request, 8));
        return resp;
    }
}
