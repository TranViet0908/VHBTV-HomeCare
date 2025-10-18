// src/main/java/Project/HouseService/Service/Customer/CustomerReviewService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorReview;
import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorReviewRepository;
import Project.HouseService.Repository.VendorServiceReviewRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class CustomerReviewService {

    private static final String STATUS_COMPLETED = "COMPLETED";

    @PersistenceContext
    private EntityManager em;

    private final UserRepository userRepository;
    private final ServiceOrderRepository orderRepository;
    private final ServiceOrderItemRepository orderItemRepository;
    private final VendorReviewRepository vendorReviewRepository;
    private final VendorServiceReviewRepository vendorServiceReviewRepository;

    public CustomerReviewService(UserRepository userRepository,
                                 ServiceOrderRepository orderRepository,
                                 ServiceOrderItemRepository orderItemRepository,
                                 VendorReviewRepository vendorReviewRepository,
                                 VendorServiceReviewRepository vendorServiceReviewRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.vendorReviewRepository = vendorReviewRepository;
        this.vendorServiceReviewRepository = vendorServiceReviewRepository;
    }

    // ======== Helpers ========
    public long requireUserIdByUsername(String username) {
        Optional<User> u = userRepository.findByUsername(username);
        if (u.isEmpty()) throw new IllegalArgumentException("User not found");
        return u.get().getId();
    }

    private void assertRatingInRange(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");
        }
    }

    // ======== Vendor-level review (đánh giá Vendor theo order) ========
    @Transactional
    public Map<String, Object> createVendorReview(long userId, long orderId, int rating, String content) {
        assertRatingInRange(rating);

        ServiceOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Đơn hàng không tồn tại"));

        Long orderCustomerId = order.getCustomerId();
        String status = order.getStatus();
        if (orderCustomerId == null || !orderCustomerId.equals(userId)) {
            throw new SecurityException("Không có quyền đánh giá đơn hàng này");
        }
        if (status == null || !STATUS_COMPLETED.equals(status)) {
            throw new IllegalStateException("Chỉ đánh giá sau khi đơn đã hoàn tất");
        }

        Long dupCount = em.createQuery(
                        "select count(v.id) from VendorReview v " +
                                "where v.serviceOrderId = :oid and v.customerId = :uid", Long.class)
                .setParameter("oid", orderId)
                .setParameter("uid", userId)
                .getSingleResult();
        if (dupCount != null && dupCount > 0) {
            throw new IllegalStateException("Đơn này đã được bạn đánh giá");
        }

        Long vendorUserId = order.getVendorId();
        if (vendorUserId == null) {
            Long anyVendorUserId = em.createQuery(
                            "select soi.vendorId from ServiceOrderItem soi where soi.serviceOrderId = :oid",
                            Long.class)
                    .setParameter("oid", orderId)
                    .setMaxResults(1)
                    .getResultStream().findFirst()
                    .orElseThrow(() -> new IllegalStateException("Không xác định được vendor của đơn"));
            vendorUserId = anyVendorUserId;
        }

        VendorReview rv = new VendorReview();
        rv.setVendorId(vendorUserId);
        rv.setCustomerId(userId);
        rv.setServiceOrderId(orderId);
        rv.setRating(rating);
        rv.setContent(content != null ? content : "");
        rv.setHidden(false);

        try {
            vendorReviewRepository.save(rv);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Bạn đã đánh giá đơn này", ex);
        }

        updateVendorRatingStatsByVendorUserId(vendorUserId);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("vendorUserId", vendorUserId);
        res.put("orderId", orderId);
        return res;
    }

    // ======== Service-level review (đánh giá gói theo order item) ========
    @Transactional
    public Map<String, Object> createServiceReview(long userId, long soItemId, int rating, String content) {
        assertRatingInRange(rating);

        ServiceOrderItem item = orderItemRepository.findById(soItemId)
                .orElseThrow(() -> new IllegalArgumentException("Mục dịch vụ không tồn tại"));

        Long orderId = item.getServiceOrderId();
        Long vendorServiceId = item.getVendorService() != null ? item.getVendorService().getId() : null;
        Long vendorUserId = item.getVendorId(); // vendor_profile.user_id

        if (orderId == null || vendorServiceId == null || vendorUserId == null) {
            throw new IllegalStateException("Thiếu thông tin liên kết của mục dịch vụ");
        }

        ServiceOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Đơn hàng của mục dịch vụ không tồn tại"));

        if (order.getCustomerId() == null || !order.getCustomerId().equals(userId)) {
            throw new SecurityException("Không có quyền đánh giá mục này");
        }
        if (order.getStatus() == null || !STATUS_COMPLETED.equals(order.getStatus())) {
            throw new IllegalStateException("Chỉ đánh giá sau khi đơn đã hoàn tất");
        }

        Long dupCount = em.createQuery(
                        "select count(r.id) from VendorServiceReview r " +
                                "where r.serviceOrderItemId = :soi and r.customerId = :uid", Long.class)
                .setParameter("soi", soItemId)
                .setParameter("uid", userId)
                .getSingleResult();
        if (dupCount != null && dupCount > 0) {
            throw new IllegalStateException("Mục dịch vụ này đã được bạn đánh giá");
        }

        VendorServiceReview r = new VendorServiceReview();
        r.setVendorServiceId(vendorServiceId);
        r.setCustomerId(userId);
        r.setServiceOrderItemId(soItemId);
        r.setRating(rating);
        r.setContent(content != null ? content : "");
        r.setHidden(false);
        r.setVendorId(vendorUserId);

        try {
            vendorServiceReviewRepository.save(r);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException("Bạn đã đánh giá mục này", ex);
        }

        updateVendorRatingStatsByVendorUserId(vendorUserId);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("vendorUserId", vendorUserId);
        res.put("vendorServiceId", vendorServiceId);
        res.put("soItemId", soItemId);
        return res;
    }

    // ======== Danh sách review (phân trang thủ công) ========
    @Transactional
    public Page<VendorReview> listVendorReviews(long vendorUserId, int page, int size) {
        int offset = page * size;

        List<VendorReview> data = em.createQuery(
                        "select v from VendorReview v " +
                                "where v.vendorId = :vid and v.hidden = false " +
                                "order by v.createdAt desc", VendorReview.class)
                .setParameter("vid", vendorUserId)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        Long total = em.createQuery(
                        "select count(v.id) from VendorReview v " +
                                "where v.vendorId = :vid and v.hidden = false", Long.class)
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        return new PageImpl<>(data, PageRequest.of(page, size), total == null ? 0 : total);
    }

    @Transactional
    public Page<VendorServiceReview> listServiceReviews(long vendorServiceId, int page, int size) {
        int offset = page * size;

        List<VendorServiceReview> data = em.createQuery(
                        "select r from VendorServiceReview r " +
                                "where r.vendorServiceId = :vsId and r.hidden = false " +
                                "order by r.createdAt desc", VendorServiceReview.class)
                .setParameter("vsId", vendorServiceId)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        Long total = em.createQuery(
                        "select count(r.id) from VendorServiceReview r " +
                                "where r.vendorServiceId = :vsId and r.hidden = false", Long.class)
                .setParameter("vsId", vendorServiceId)
                .getSingleResult();

        return new PageImpl<>(data, PageRequest.of(page, size), total == null ? 0 : total);
    }

    // ======== Cập nhật thống kê vào vendor_profile (user_id) ========
    @Transactional
    public void updateVendorRatingStatsByVendorUserId(long vendorUserId) {
        Object[] row = em.createQuery(
                        "select coalesce(avg(v.rating),0), count(v.id) from VendorReview v " +
                                "where v.vendorId = :vid and v.hidden = false", Object[].class)
                .setParameter("vid", vendorUserId)
                .getSingleResult();

        BigDecimal avg = BigDecimal.ZERO;
        long cnt = 0L;
        if (row != null) {
            Double a = (Double) row[0];
            Long c = (Long) row[1];
            if (a != null) avg = BigDecimal.valueOf(a).setScale(2, RoundingMode.HALF_UP);
            if (c != null) cnt = c;
        }

        em.createNativeQuery("UPDATE vendor_profile SET rating_avg = :avg, rating_count = :cnt WHERE user_id = :uid")
                .setParameter("avg", avg)
                .setParameter("cnt", (int) Math.min(cnt, Integer.MAX_VALUE))
                .setParameter("uid", vendorUserId)
                .executeUpdate();
    }

    // ======== Guards để bật/tắt form trên UI ========
    @Transactional
    public boolean canReviewVendor(long userId, long orderId) {
        Long ok = em.createQuery(
                        "select count(o.id) from ServiceOrder o " +
                                "where o.id = :oid and o.customerId = :uid and o.status = :st", Long.class)
                .setParameter("oid", orderId)
                .setParameter("uid", userId)
                .setParameter("st", STATUS_COMPLETED)
                .getSingleResult();
        return ok != null && ok > 0;
    }

    @Transactional
    public boolean canReviewService(long userId, long soItemId) {
        Long ok = em.createQuery(
                        "select count(soi.id) from ServiceOrderItem soi, ServiceOrder o " +
                                "where soi.id = :soi and o.id = soi.serviceOrderId " +
                                "and o.customerId = :uid and o.status = :st", Long.class)
                .setParameter("soi", soItemId)
                .setParameter("uid", userId)
                .setParameter("st", STATUS_COMPLETED)
                .getSingleResult();
        return ok != null && ok > 0;
    }
    // Thêm vào class CustomerReviewService
    @Transactional
    public java.util.List<Project.HouseService.Entity.ServiceOrderItem>
    listEligibleItems(long userId, long vendorServiceId) {
        return em.createQuery(
                        "select soi from ServiceOrderItem soi, ServiceOrder o " +
                                "where o.id = soi.serviceOrderId " +
                                "and o.customerId = :uid " +
                                "and o.status = :st " +
                                "and soi.vendorService.id = :vsId " +
                                "and not exists (select 1 from VendorServiceReview r " +
                                "               where r.serviceOrderItemId = soi.id " +
                                "                 and r.customerId = :uid)",
                        Project.HouseService.Entity.ServiceOrderItem.class)
                .setParameter("uid", userId)
                .setParameter("st", STATUS_COMPLETED) // "COMPLETED"
                .setParameter("vsId", vendorServiceId)
                .getResultList();
    }
    @Transactional
    public List<ServiceOrder> listEligibleOrders(long userId, long vendorUserId) {
        // Đơn thuộc user + của vendor này + COMPLETED + chưa có VendorReview bởi user
        return em.createQuery(
                        "select o from ServiceOrder o " +
                                "where o.customerId = :uid and o.vendorId = :vid and o.status = :st " +
                                "and not exists (select 1 from VendorReview v " +
                                "                where v.serviceOrderId = o.id and v.customerId = :uid)",
                        ServiceOrder.class)
                .setParameter("uid", userId)
                .setParameter("vid", vendorUserId)
                .setParameter("st", "COMPLETED")
                .getResultList();
    }
    @Transactional
    public Map<String, Object> createVendorReviewByVendor(long userId, long vendorUserId, int rating, String content) {
        // validate
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("Rating phải 1..5");

        // tìm 1 đơn COMPLETED gần nhất của user với vendor này mà chưa có review
        Long orderId = em.createQuery(
                        "select o.id from ServiceOrder o " +
                                "where o.customerId = :uid and o.vendorId = :vid and o.status = :st " +
                                "and not exists (select 1 from VendorReview v where v.serviceOrderId = o.id and v.customerId = :uid) " +
                                "order by o.id desc", Long.class)
                .setParameter("uid", userId)
                .setParameter("vid", vendorUserId)
                .setParameter("st", "COMPLETED")
                .setMaxResults(1)
                .getResultStream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Chưa có đơn COMPLETED nào chưa đánh giá với nhà cung cấp này"));

        // tạo review
        Project.HouseService.Entity.VendorReview rv = new Project.HouseService.Entity.VendorReview();
        rv.setVendorId(vendorUserId);
        rv.setCustomerId(userId);
        rv.setServiceOrderId(orderId);
        rv.setRating(rating);
        rv.setContent(content != null ? content : "");
        rv.setHidden(false);

        vendorReviewRepository.save(rv);

        // cập nhật thống kê
        updateVendorRatingStatsByVendorUserId(vendorUserId);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("vendorUserId", vendorUserId);
        res.put("orderId", orderId);
        return res;
    }

    public List<ReviewableOrder> findReviewableVendorOrders(long customerId, long vendorUserId) {
        String sql = """
    select distinct o.id as id, max(i.scheduled_at) as last_time
    from service_order_item i
    join service_order o on o.id = i.service_order_id
    where o.customer_id = :cid
      and i.vendor_id   = :vid
      and o.status      = 'COMPLETED'
      and o.id not in (select coalesce(v.service_order_id, -1)
                       from vendor_review v
                       where v.customer_id = :cid and v.vendor_id = :vid)
    group by o.id
    order by last_time desc
  """;
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("cid", customerId)
                .setParameter("vid", vendorUserId)
                .getResultList();
        return rows.stream()
                .map(r -> new ReviewableOrder(((Number) r[0]).longValue(),
                        ((java.sql.Timestamp) r[1]).toLocalDateTime()))
                .toList();
    }

    public record ReviewableOrder(Long id, java.time.LocalDateTime lastTime) {}
    @Transactional
    public boolean hasCompletedOrderWithVendor(long userId, long vendorUserId) {
        Long n = em.createQuery(
                        "select count(distinct o.id) " +
                                "from ServiceOrder o, ServiceOrderItem i " +
                                "where o.id = i.serviceOrderId " +
                                "and o.customerId = :uid " +
                                "and i.vendorId   = :vid " +
                                "and o.status     = :st", Long.class)
                .setParameter("uid", userId)
                .setParameter("vid", vendorUserId)
                .setParameter("st", "COMPLETED")
                .getSingleResult();
        return n != null && n > 0;
    }

    @Transactional
    public Map<Long,String> mapUsernamesByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) return java.util.Collections.emptyMap();
        var users = userRepository.findAllById(ids);
        Map<Long,String> m = new java.util.HashMap<>();
        for (User u : users) m.put(u.getId(), u.getUsername());
        return m;
    }

}
