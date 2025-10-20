// src/main/java/Project/HouseService/Controller/Customer/NotificationController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Service.Customer.CustomerNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.*;

@Controller
@RequestMapping("/customer/notifications")
public class NotificationController {

    private final CustomerNotificationService app;

    public NotificationController(CustomerNotificationService app) {
        this.app = app;
    }

    // Giữ link header: /customer/notifications/page
    @GetMapping({"", "/"})
    public String indexRedirect() {
        return "redirect:/customer/notifications/page";
    }

    // Trang chính
    @GetMapping("/page")
    @Transactional(readOnly = true)
    public String page(Authentication auth, Model model,
                       @RequestParam(defaultValue = "20") int limit,
                       @RequestParam(defaultValue = "0") int offset) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        List<Map<String, Object>> items = app.listModels(userId, limit, offset);
        long unread = app.countUnread(userId);
        Map<String, Long> typeUnread = countUnreadByType(items);

        model.addAttribute("notifications", items);
        model.addAttribute("unreadCount", unread);
        model.addAttribute("orderNotifCount", typeUnread.getOrDefault("ORDER", 0L));
        model.addAttribute("paymentNotifCount", typeUnread.getOrDefault("PAYMENT", 0L));
        model.addAttribute("wishlistNotifCount", typeUnread.getOrDefault("WISHLIST", 0L));
        model.addAttribute("promoNotifCount", typeUnread.getOrDefault("PROMOTION", 0L));
        model.addAttribute("limit", limit);
        model.addAttribute("offset", offset);

        // Khớp file: templates/customer/notification/index.html
        return "customer/notification/index";
    }

    private Map<String, Long> countUnreadByType(List<Map<String, Object>> items) {
        Map<String, Long> m = new HashMap<>();
        for (Map<String, Object> it : items) {
            String type = String.valueOf(it.get("type"));
            boolean isRead = Boolean.TRUE.equals(it.get("isRead"));
            if (!isRead) m.put(type, m.getOrDefault(type, 0L) + 1);
        }
        return m;
    }

    // SSE
    @GetMapping(path = "/sse", produces = "text/event-stream")
    public SseEmitter sse(Authentication auth) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        return app.subscribe(userId);
    }

    // REST
    @GetMapping("/list")
    @ResponseBody
    public List<Map<String, Object>> list(Authentication auth,
                                          @RequestParam(defaultValue = "20") int limit,
                                          @RequestParam(defaultValue = "0") int offset) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        return app.listModels(userId, limit, offset);
    }

    @GetMapping("/unread-count")
    @ResponseBody
    public Map<String, Object> unread(Authentication auth) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        return Map.of("count", app.countUnread(userId));
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<?> markRead(Authentication auth, @PathVariable("id") Long id) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        app.markRead(userId, id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<?> markAll(Authentication auth) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        int n = app.markAllRead(userId);
        return ResponseEntity.ok(Map.of("updated", n));
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> delete(Authentication auth, @PathVariable("id") Long id) {
        Long userId = app.requireUserIdByUsername(auth.getName());
        app.softDelete(userId, id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
