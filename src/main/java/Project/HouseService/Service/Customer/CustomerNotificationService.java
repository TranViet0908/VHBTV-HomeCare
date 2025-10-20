// src/main/java/Project/HouseService/Service/Customer/CustomerNotificationService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.*;
import Project.HouseService.Entity.Notification.RelatedType;
import Project.HouseService.Entity.Notification.Type;
import Project.HouseService.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class CustomerNotificationService {

    private final NotificationRepository notifications;
    private final NotificationUserRepository notificationUsers;
    private final UserRepository users;

    // SSE emitter pool theo userId
    private final ConcurrentMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public CustomerNotificationService(NotificationRepository notifications,
                                       NotificationUserRepository notificationUsers,
                                       UserRepository users) {
        this.notifications = notifications;
        this.notificationUsers = notificationUsers;
        this.users = users;
    }

    // ======= Helpers =======
    public Long requireUserIdByUsername(String username) {
        return users.findByUsername(username)
                .map(User::getId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
    }

    private Map<String, Object> toModel(NotificationUser nu) {
        Notification n = nu.getNotification();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", nu.getId());
        m.put("type", n.getType().name());
        m.put("title", n.getTitle());
        m.put("message", n.getMessage());
        m.put("createdAt", n.getCreatedAt());
        m.put("isRead", nu.isRead());
        m.put("relatedType", n.getRelatedType() != null ? n.getRelatedType().name() : null);
        m.put("relatedId", n.getRelatedId());
        return m;
    }

    private void pushEvent(Long userId, String name, Object data) {
        List<SseEmitter> list = emitters.getOrDefault(userId, new CopyOnWriteArrayList<>());
        for (SseEmitter em : list) {
            try {
                em.send(SseEmitter.event().name(name).data(data));
            } catch (IOException e) {
                em.completeWithError(e);
            }
        }
    }

    // ======= Queries =======
    @Transactional(readOnly = true)
    public List<Map<String, Object>> listModels(Long userId, int limit, int offset) {
        int page = offset / Math.max(1, limit);
        List<NotificationUser> rows = notificationUsers.findPageByUser(userId, PageRequest.of(page, limit));
        List<Map<String, Object>> list = new ArrayList<>(rows.size());
        for (NotificationUser nu : rows) list.add(toModel(nu));
        return list;
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationUsers.countByUser_IdAndIsDeletedFalseAndIsReadFalse(userId);
    }

    // ======= Commands =======
    @Transactional
    public void markRead(Long userId, Long notifUserId) {
        NotificationUser nu = notificationUsers.findByIdAndUser_Id(notifUserId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!nu.isRead()) {
            nu.setRead(true);
            nu.setReadAt(Instant.now());
        }
        // đẩy badge mới
        pushEvent(userId, "unread-count", countUnread(userId));
    }

    @Transactional
    public int markAllRead(Long userId) {
        int n = notificationUsers.markAllRead(userId);
        // đẩy badge mới
        pushEvent(userId, "unread-count", 0);
        return n;
    }

    @Transactional
    public void softDelete(Long userId, Long notifUserId) {
        NotificationUser nu = notificationUsers.findByIdAndUser_Id(notifUserId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        nu.setDeleted(true);
        // cập nhật badge nếu xóa 1 thông báo chưa đọc
        if (!nu.isRead()) pushEvent(userId, "unread-count", countUnread(userId));
    }

    @Transactional
    public Long createForUser(Long toUserId, Type type, String title, String message,
                              RelatedType relatedType, Long relatedId,
                              String dataJson, Long actorUserId) {
        Notification n = new Notification();
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedType(relatedType);
        n.setRelatedId(relatedId);
        n.setDataJson(dataJson);
        if (actorUserId != null) {
            users.findById(actorUserId).ifPresent(n::setActorUser);
        }
        notifications.save(n);

        NotificationUser nu = new NotificationUser();
        nu.setNotification(n);
        users.findById(toUserId).ifPresent(nu::setUser);
        nu.setDeliveredAt(Instant.now());
        notificationUsers.save(nu);

        Map<String, Object> payload = toModel(nu);
        pushEvent(toUserId, "notification", payload);
        pushEvent(toUserId, "unread-count", countUnread(toUserId));
        return nu.getId();
    }

    // ======= SSE =======
    @Transactional(readOnly = true)
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(30L * 60 * 1000); // 30 phút
        emitters.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).remove(emitter));
        emitter.onTimeout(() -> emitters.getOrDefault(userId, new CopyOnWriteArrayList<>()).remove(emitter));
        try {
            // Gửi badge hiện tại ngay khi kết nối
            emitter.send(SseEmitter.event().name("unread-count").data(countUnread(userId)));
        } catch (IOException ignored) {}
        return emitter;
    }
}
