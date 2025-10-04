// src/main/java/Project/HouseService/Controller/Customer/ServicesPage/ServicesIndexController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.Service;
import Project.HouseService.Service.Customer.ServicesIndexService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class ServicesIndexController {

    private final ServicesIndexService svc;

    public ServicesIndexController(ServicesIndexService svc) {
        this.svc = svc;
    }

    @GetMapping("/services")
    public String indexHtml(@RequestParam(value = "q", required = false) String q,
                            @RequestParam(value = "parentId", required = false) Long parentId,
                            Authentication authentication,
                            Model model) {

        Long customerId = extractCustomerId(authentication);

        // Roots + children
        List<Service> roots = svc.listRoot(q);
        if (parentId != null) {
            roots = roots.stream().filter(r -> Objects.equals(r.getId(), parentId)).collect(Collectors.toList());
        }

        Map<Long, List<Service>> groupChildren = new LinkedHashMap<>();
        List<Long> allChildIds = new ArrayList<>();
        for (Service root : roots) {
            List<Service> children = svc.listChildren(root.getId(), q);
            if (!children.isEmpty()) {
                groupChildren.put(root.getId(), children);
                children.forEach(c -> allChildIds.add(c.getId()));
            }
        }
        var enrich = svc.enrichByServiceIds(allChildIds);

        // Sections
        List<Map<String, Object>> sections = new ArrayList<>();
        for (Service root : roots) {
            List<Service> children = groupChildren.getOrDefault(root.getId(), Collections.emptyList());
            if (children.isEmpty()) continue;
            Map<String, Object> sec = new LinkedHashMap<>();
            sec.put("rootId", root.getId());
            sec.put("rootName", root.getName());
            sec.put("rootDesc", root.getDescription());
            sec.put("cards", svc.toCards(children, enrich));
            sections.add(sec);
        }

        // Suggestions (ẩn khi không đăng nhập vì customerId == null)
        var suggestions = svc.suggestions(customerId, 8);

        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("parentId", parentId);
        model.addAttribute("roots", roots);
        model.addAttribute("sections", sections);
        model.addAttribute("suggestions", suggestions);

        return "customer/services/services";
    }

    @GetMapping("/api/customer/services")
    @ResponseBody
    public Map<String, Object> indexApi(@RequestParam(value = "q", required = false) String q,
                                        @RequestParam(value = "parentId", required = false) Long parentId,
                                        Authentication authentication) {
        Long customerId = extractCustomerId(authentication);

        List<Service> roots = svc.listRoot(q);
        if (parentId != null) {
            roots = roots.stream().filter(r -> Objects.equals(r.getId(), parentId)).collect(Collectors.toList());
        }
        Map<Long, List<Service>> groupChildren = new LinkedHashMap<>();
        List<Long> allChildIds = new ArrayList<>();
        for (Service root : roots) {
            List<Service> children = svc.listChildren(root.getId(), q);
            if (!children.isEmpty()) {
                groupChildren.put(root.getId(), children);
                children.forEach(c -> allChildIds.add(c.getId()));
            }
        }
        var enrich = svc.enrichByServiceIds(allChildIds);

        List<Map<String, Object>> payload = new ArrayList<>();
        for (Service root : roots) {
            List<Service> children = groupChildren.getOrDefault(root.getId(), Collections.emptyList());
            if (children.isEmpty()) continue;
            Map<String, Object> sec = new LinkedHashMap<>();
            sec.put("root", Map.of("id", root.getId(), "name", root.getName()));
            sec.put("items", svc.toCards(children, enrich));
            payload.add(sec);
        }

        var suggestions = svc.suggestions(customerId, 8);

        return Map.of(
                "q", q,
                "parentId", parentId,
                "sections", payload,
                "suggestions", suggestions
        );
    }

    /** Trích customerId an toàn khi đăng xuất hoặc anonymousUser. */
    private Long extractCustomerId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return null;
        Object principal = auth.getPrincipal();
        if (principal == null) return null;

        // anonymousUser là String
        if (principal instanceof String) return null;

        // Nếu principal có getId(): gọi phản chiếu
        try {
            Method m = principal.getClass().getMethod("getId");
            Object v = m.invoke(principal);
            if (v instanceof Number) return ((Number) v).longValue();
        } catch (Exception ignored) {}

        // Nếu principal là entity User của dự án
        try {
            if (principal instanceof Project.HouseService.Entity.User u) {
                return u.getId();
            }
        } catch (Throwable ignored) {}

        return null;
    }
}
