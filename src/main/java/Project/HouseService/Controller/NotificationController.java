//package Project.HouseService.Controller;
//
//import Project.HouseService.Entity.Notification;
//import Project.HouseService.Service.Admin.UserService;
//import Project.HouseService.Service.NotificationService;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Objects;
//import java.util.stream.Collectors;
//
//@Controller
//public class NotificationController {
//
//    private final NotificationService notificationService;
//    private final UserService userService;
//
//    public NotificationController(NotificationService notificationService, UserService userService) {
//        this.notificationService = notificationService;
//        this.userService = userService;
//    }
//
//    // Helper: map topic -> URL chi tiết
//    private String buildRedirectForNotification(Notification n) {
//        if (n == null || n.getRelatedId() == null || n.getTopic() == null) return null;
//
//        Long rid = n.getRelatedId();
//        switch (n.getTopic()) {
//            case APPROVE:
//                return "redirect:/user/contracts/approvals/" + rid;   // ví dụ: phê duyệt 1
//            case PROJECT:
//                return "redirect:/user/contracts/projects/" + rid;   // trang chi tiết dự án
//            case PAYMENT:
//                return "redirect:/user/contracts/payments/";   // trang chi tiết thanh toán
//            case CONTRACT:
//                return "redirect:/user/contracts";  // trang chi tiết hợp đồng
//            default:
//                return null;
//        }
//    }
//
//    // ========================= USER =========================
//
//    @GetMapping("/user/notifications")
//    public String userList(@RequestParam(value = "type", required = false) String type,
//                           @RequestParam(value = "unread", required = false) Boolean unread,
//                           Authentication auth,
//                           Model model) {
//        Long userId = currentUserId(auth);
//        List<Notification> notifications = notificationService.getNotificationsByUser(userId, type, unread);
//        long unreadCount = notificationService.countUnreadByUser(userId);
//
//        model.addAttribute("notifications", notifications);
//        model.addAttribute("unreadCount", unreadCount);
//        model.addAttribute("filterType", type);
//        model.addAttribute("filterUnread", Boolean.TRUE.equals(unread));
//        return "user/notifications/index";
//    }
//
//    @PostMapping("/user/notifications/read-all")
//    public String userReadAll(Authentication auth, RedirectAttributes ra) {
//        notificationService.markAllRead(currentUserId(auth));
//        ra.addFlashAttribute("msg", "Đã đánh dấu tất cả đã đọc");
//        return "redirect:/user/notifications";
//    }
//
//    @GetMapping("/user/notifications/open/{id}")
//    public String userOpen(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
//        Long userId = currentUserId(auth);
//        // markRead() đã kiểm tra quyền sở hữu, sẽ throw nếu không phải của user
//        Notification n = notificationService.markRead(id, userId);
//
//        String target = buildRedirectForNotification(n);
//        if (target != null) return target;
//
//        ra.addFlashAttribute("msg", "Thông báo không có liên kết chi tiết.");
//        return "redirect:/user/notifications";
//    }
//
//    @GetMapping("/user/notifications/delete/{id}")
//    public String userDelete(@PathVariable Long id, Authentication auth, RedirectAttributes ra) {
//        Long userId = currentUserId(auth);
//        Notification n = notificationService.getNotificationById(id);
//        if (Objects.equals(n.getRecipientUserId(), userId)) {
//            notificationService.deleteNotification(id);
//            ra.addFlashAttribute("msg", "Đã xóa thông báo");
//        } else {
//            ra.addFlashAttribute("msg", "Không có quyền xóa thông báo này");
//        }
//        return "redirect:/user/notifications";
//    }
//
//    // ========================= ADMIN =========================
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/notifications")
//    public String adminList(@RequestParam(required = false) String search,
//                            @RequestParam(required = false) String topic,
//                            @RequestParam(required = false) Boolean isRead,
//                            @RequestParam(required = false) Long recipientUserId,
//                            Model model) {
//        // Service không có adminSearch → lọc tại controller
//        List<Notification> all = notificationService.getAllNotifications();
//        List<Notification> filtered = all.stream()
//                .filter(n -> recipientUserId == null || Objects.equals(n.getRecipientUserId(), recipientUserId))
//                .filter(n -> topic == null || topic.isBlank() || (n.getTopic() != null && n.getTopic().name().equalsIgnoreCase(topic)))
//                .filter(n -> isRead == null || (n.isRead() == isRead))
//                .filter(n -> {
//                    if (search == null || search.isBlank()) return true;
//                    String s = search.trim().toLowerCase();
//                    String inTitle = n.getTitle() != null ? n.getTitle().toLowerCase() : "";
//                    String inMsg = n.getMessage() != null ? n.getMessage().toLowerCase() : "";
//                    String inId = String.valueOf(n.getId());
//                    String inRelated = n.getRelatedId() != null ? String.valueOf(n.getRelatedId()) : "";
//                    return inTitle.contains(s) || inMsg.contains(s) || inId.equals(s) || inRelated.equals(s);
//                })
//                .collect(Collectors.toList());
//
//        model.addAttribute("notifications", filtered);
//        model.addAttribute("search", search);
//        model.addAttribute("topic", topic);
//        model.addAttribute("isRead", isRead);
//        model.addAttribute("recipientUserId", recipientUserId);
//        return "admin/notifications/list";
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/notifications/create")
//    public String adminCreateForm(Model model) {
//        model.addAttribute("notification", new Notification());
//        return "admin/notifications/create";
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin/notifications/create")
//    public String adminCreate(@ModelAttribute Notification notification, RedirectAttributes ra) {
//        notificationService.createNotification(notification);
//        ra.addFlashAttribute("msg", "Đã tạo thông báo");
//        return "redirect:/admin/notifications";
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/notifications/{id}")
//    public String adminEditForm(@PathVariable Long id, Model model) {
//        model.addAttribute("notification", notificationService.getNotificationById(id));
//        return "admin/notifications/edit";
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping("/admin/notifications/{id}")
//    public String adminUpdate(@PathVariable Long id,
//                              @ModelAttribute Notification notification,
//                              RedirectAttributes ra) {
//        notificationService.updateNotification(notification, id);
//        ra.addFlashAttribute("msg", "Đã cập nhật thông báo");
//        return "redirect:/admin/notifications";
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/notifications/toggle/{id}")
//    public String adminToggle(@PathVariable Long id, @RequestParam boolean read) {
//        Notification n = notificationService.getNotificationById(id);
//        n.setRead(read);
//        n.setReadAt(read ? (n.getReadAt() != null ? n.getReadAt() : LocalDateTime.now()) : null);
//        notificationService.updateNotification(n, id);
//        return "redirect:/admin/notifications";
//    }
//
//    @PreAuthorize("hasRole('ADMIN')")
//    @GetMapping("/admin/notifications/delete/{id}")
//    public String adminDelete(@PathVariable Long id, RedirectAttributes ra) {
//        notificationService.deleteNotification(id);
//        ra.addFlashAttribute("msg", "Đã xóa thông báo");
//        return "redirect:/admin/notifications";
//    }
//
//    // ========================= HELPERS =========================
//
//    private Long currentUserId(Authentication authentication) {
//        String username = authentication.getName();
//        var user = userService.findByUsername(username);
//        return user.getId();
//    }
//}
