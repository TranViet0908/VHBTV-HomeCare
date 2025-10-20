// src/main/java/Project/HouseService/Service/Customer/NotificationScannerService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.Notification;
import Project.HouseService.Entity.Notification.RelatedType;
import Project.HouseService.Entity.Notification.Type;
import Project.HouseService.Repository.NotificationRepository;
import Project.HouseService.Repository.NotificationUserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationScannerService {

    @PersistenceContext
    private EntityManager em;

    private final CustomerNotificationService notifier;
    private final NotificationRepository notificationRepo;
    private final NotificationUserRepository notificationUserRepo;

    private volatile long lastPaymentId = 0L;
    private volatile long lastCouponUserId = 0L;
    private volatile Instant lastOrderUpdatedAt = Instant.EPOCH;
    private volatile boolean initialized = false;

    public NotificationScannerService(CustomerNotificationService notifier,
                                      NotificationRepository notificationRepo,
                                      NotificationUserRepository notificationUserRepo) {
        this.notifier = notifier;
        this.notificationRepo = notificationRepo;
        this.notificationUserRepo = notificationUserRepo;
    }

    @PostConstruct
    public void initCursor() {
        try {
            Long maxPay = toLong(em.createNativeQuery("SELECT IFNULL(MAX(id),0) FROM payment").getSingleResult());
            Long maxCu  = toLong(em.createNativeQuery("SELECT IFNULL(MAX(id),0) FROM coupon_user").getSingleResult());
            java.sql.Timestamp ts = (java.sql.Timestamp) em
                    .createNativeQuery("SELECT COALESCE(MAX(updated_at), NOW()) FROM service_order")
                    .getSingleResult();
            lastPaymentId = maxPay == null ? 0L : maxPay;
            lastCouponUserId = maxCu == null ? 0L : maxCu;
            lastOrderUpdatedAt = ts != null ? ts.toInstant() : Instant.now();
        } catch (Exception ignored) { /* nếu chưa có bảng thì bỏ qua */ }
        initialized = true;
    }

    // KHÔNG @Transactional ở đây để tránh rollback toàn bộ khi 1 nguồn lỗi
    @Scheduled(fixedDelay = 10_000L, initialDelay = 5_000L)
    public void scanAll() {
        if (!initialized) initCursor();
        try { scanPayments(); } catch (Exception ignored) {}
        try { scanCouponUsers(); } catch (Exception ignored) {}
        try { scanOrders(); } catch (Exception ignored) {}
    }

    // ===== PAYMENT: status = 'PAID', SERVICE_ORDER =====
    @SuppressWarnings("unchecked")
    private void scanPayments() {
        // payment: id, user_id, pay_target_type('SERVICE_ORDER'|'PRODUCT_ORDER'), pay_target_id, status('PAID')
        List<Object[]> rows = em.createNativeQuery("""
            SELECT p.id AS payment_id,
                   p.pay_target_id AS order_id,
                   CASE WHEN p.pay_target_type = 'SERVICE_ORDER' THEN o.customer_id ELSE p.user_id END AS user_id
            FROM payment p
            LEFT JOIN service_order o ON (p.pay_target_type = 'SERVICE_ORDER' AND o.id = p.pay_target_id)
            WHERE p.status = 'PAID' AND p.id > :lastId
            ORDER BY p.id ASC
            LIMIT 200
        """).setParameter("lastId", lastPaymentId).getResultList();

        for (Object[] r : rows) {
            Long paymentId = toLong(r[0]);
            Long orderId   = toLong(r[1]);
            Long userId    = toLong(r[2]);
            if (paymentId == null || userId == null) { lastPaymentId = paymentId != null ? paymentId : lastPaymentId; continue; }

            if (!existsNotif(userId, Type.PAYMENT, RelatedType.PAYMENT, paymentId)) {
                notifier.createForUser(
                        userId,
                        Type.PAYMENT,
                        "Thanh toán thành công",
                        "Bạn đã thanh toán đơn #" + (orderId != null ? orderId : paymentId),
                        RelatedType.PAYMENT,
                        paymentId,
                        null,
                        null
                );
            }
            lastPaymentId = paymentId;
        }
    }

    // ===== COUPON_USER: gán coupon cho user =====
    @SuppressWarnings("unchecked")
    private void scanCouponUsers() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT cu.id AS link_id, cu.coupon_id, cu.user_id, c.code
            FROM coupon_user cu
            JOIN coupon c ON c.id = cu.coupon_id
            WHERE cu.id > :lastId
            ORDER BY cu.id ASC
            LIMIT 200
        """).setParameter("lastId", lastCouponUserId).getResultList();

        for (Object[] r : rows) {
            Long linkId   = toLong(r[0]);
            Long couponId = toLong(r[1]);
            Long userId   = toLong(r[2]);
            String code   = r[3] != null ? r[3].toString() : "COUPON";
            if (linkId == null || userId == null || couponId == null) { lastCouponUserId = linkId != null ? linkId : lastCouponUserId; continue; }

            if (!existsNotif(userId, Type.PROMOTION, RelatedType.COUPON, couponId)) {
                notifier.createForUser(
                        userId,
                        Type.PROMOTION,
                        "Tặng mã khuyến mại",
                        "Bạn nhận được mã: " + code,
                        RelatedType.COUPON,
                        couponId,
                        null,
                        null
                );
            }
            lastCouponUserId = linkId;
        }
    }

    // ===== SERVICE_ORDER: status change (dựa vào updated_at) =====
    @SuppressWarnings("unchecked")
    private void scanOrders() {
        List<Object[]> rows = em.createNativeQuery("""
            SELECT o.id AS order_id, o.customer_id AS user_id, o.status, o.updated_at
            FROM service_order o
            WHERE o.updated_at > :lastTime
            ORDER BY o.updated_at ASC
            LIMIT 200
        """).setParameter("lastTime", java.sql.Timestamp.from(lastOrderUpdatedAt)).getResultList();

        for (Object[] r : rows) {
            Long orderId  = toLong(r[0]);
            Long userId   = toLong(r[1]);
            String status = r[2] != null ? r[2].toString() : null;
            Instant updatedAt = toInstant(r[3]);
            if (orderId == null || userId == null || updatedAt == null) continue;

            boolean already = existsOrderStatusNotif(userId, orderId, status);
            if (!already) {
                notifier.createForUser(
                        userId,
                        Type.ORDER,
                        "Đơn #" + orderId + " cập nhật",
                        "Đơn hàng của bạn đã chuyển sang trạng thái " + status,
                        RelatedType.SERVICE_ORDER,
                        orderId,
                        null,
                        null
                );
            }
            if (updatedAt.isAfter(lastOrderUpdatedAt)) lastOrderUpdatedAt = updatedAt;
        }
    }

    // ===== Dedup helpers =====
    private boolean existsNotif(Long userId, Type type, RelatedType rtype, Long rid) {
        Number cnt = (Number) em.createQuery("""
            SELECT COUNT(nu.id)
            FROM Project.HouseService.Entity.NotificationUser nu
            JOIN nu.notification n
            WHERE nu.user.id = :uid
              AND nu.isDeleted = false
              AND n.type = :t
              AND n.relatedType = :rt
              AND n.relatedId = :rid
        """).setParameter("uid", userId)
                .setParameter("t", type)
                .setParameter("rt", rtype)
                .setParameter("rid", rid)
                .getSingleResult();
        return cnt != null && cnt.longValue() > 0;
    }

    private boolean existsOrderStatusNotif(Long userId, Long orderId, String status) {
        if (status == null) return existsNotif(userId, Type.ORDER, RelatedType.SERVICE_ORDER, orderId);
        String like = "%" + status + "%";
        Number cnt = (Number) em.createQuery("""
            SELECT COUNT(nu.id)
            FROM Project.HouseService.Entity.NotificationUser nu
            JOIN nu.notification n
            WHERE nu.user.id = :uid
              AND nu.isDeleted = false
              AND n.type = :t
              AND n.relatedType = :rt
              AND n.relatedId = :rid
              AND LOWER(n.message) LIKE LOWER(:like)
        """).setParameter("uid", userId)
                .setParameter("t", Type.ORDER)
                .setParameter("rt", RelatedType.SERVICE_ORDER)
                .setParameter("rid", orderId)
                .setParameter("like", like)
                .getSingleResult();
        return cnt != null && cnt.longValue() > 0;
    }

    // ===== utils =====
    private static Long toLong(Object o){ return o==null?null:((Number)o).longValue(); }
    private static Instant toInstant(Object o){
        if (o == null) return null;
        if (o instanceof java.sql.Timestamp ts) return ts.toInstant();
        if (o instanceof java.util.Date d) return d.toInstant();
        return null;
    }
}
