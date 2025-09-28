// src/main/java/Project/HouseService/Repository/ReportRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.ServiceOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;

public interface ReportRepository extends JpaRepository<ServiceOrder, Long> {

    // Doanh thu theo ngày
    @Query(value = """
              SELECT DATE(p.paid_at) d, SUM(p.amount) revenue
              FROM payment p
              WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
                AND p.paid_at >= :from AND p.paid_at < :to
              GROUP BY DATE(p.paid_at) ORDER BY d
            """, nativeQuery = true)
    List<Object[]> revenueByDay(@Param("from") Timestamp from, @Param("to") Timestamp to);

    @Query(value = """
              SELECT p.provider, COUNT(*) cnt, SUM(p.amount) revenue
              FROM payment p
              WHERE p.pay_target_type='SERVICE_ORDER' AND p.status='PAID'
                AND p.paid_at >= :from AND p.paid_at < :to
              GROUP BY p.provider ORDER BY revenue DESC
            """, nativeQuery = true)
    List<Object[]> revenueByProvider(@Param("from") Timestamp from, @Param("to") Timestamp to);

    @Query(value = """
              SELECT so.status, COUNT(*) cnt, COALESCE(SUM(so.total),0) total_amount
              FROM service_order so
              WHERE so.created_at >= :from AND so.created_at < :to
              GROUP BY so.status ORDER BY cnt DESC
            """, nativeQuery = true)
    List<Object[]> ordersByStatus(@Param("from") Timestamp from, @Param("to") Timestamp to);

    @Query(value = """
              SELECT so.vendor_id, COALESCE(vp.display_name,'(N/A)') vendor_name,
                     SUM(p.amount) revenue, COUNT(DISTINCT so.id) orders
              FROM service_order so
              JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id=so.id AND p.status='PAID'
              LEFT JOIN vendor_profile vp ON vp.user_id=so.vendor_id
              WHERE p.paid_at >= :from AND p.paid_at < :to
              GROUP BY so.vendor_id, vendor_name
              ORDER BY revenue DESC
              LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> topVendors(@Param("from") Timestamp from, @Param("to") Timestamp to, @Param("limit") int limit);

    @Query(value = """
              SELECT soi.vendor_service_id, COALESCE(vs.title,'(N/A)') title,
                     SUM(soi.subtotal) amount, SUM(soi.quantity) qty, COUNT(DISTINCT so.id) orders
              FROM service_order_item soi
              JOIN service_order so ON so.id=soi.service_order_id
              LEFT JOIN vendor_service vs ON vs.id=soi.vendor_service_id
              WHERE so.created_at >= :from AND so.created_at < :to
              GROUP BY soi.vendor_service_id, title
              ORDER BY amount DESC
              LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> topServices(@Param("from") Timestamp from, @Param("to") Timestamp to, @Param("limit") int limit);

    @Query(value = """
              SELECT c.code, COUNT(*) uses, COALESCE(SUM(cr.amount_discounted),0) discounted
              FROM coupon_redemption cr
              JOIN coupon c ON c.id=cr.coupon_id
              WHERE cr.target_type='SERVICE_ORDER' AND cr.redeemed_at >= :from AND cr.redeemed_at < :to
              GROUP BY c.code ORDER BY discounted DESC
            """, nativeQuery = true)
    List<Object[]> couponUsage(@Param("from") Timestamp from, @Param("to") Timestamp to);

    @Query(value = """
              SELECT vr.vendor_id, COALESCE(vp.display_name,'(N/A)') vendor_name,
                     ROUND(AVG(vr.rating),2) avg_rating, COUNT(*) reviews
              FROM vendor_review vr
              LEFT JOIN vendor_profile vp ON vp.user_id=vr.vendor_id
              WHERE vr.rating IS NOT NULL AND vr.created_at >= :from AND vr.created_at < :to
              GROUP BY vr.vendor_id, vendor_name
              ORDER BY avg_rating DESC, reviews DESC
            """, nativeQuery = true)
    List<Object[]> vendorRatings(@Param("from") Timestamp from, @Param("to") Timestamp to);

    @Query(value = """
              SELECT vsr.vendor_service_id, COALESCE(vs.title,'(N/A)') title,
                     ROUND(AVG(vsr.rating),2) avg_rating, COUNT(*) reviews
              FROM vendor_service_review vsr
              LEFT JOIN vendor_service vs ON vs.id=vsr.vendor_service_id
              WHERE vsr.rating IS NOT NULL AND vsr.created_at >= :from AND vsr.created_at < :to
              GROUP BY vsr.vendor_service_id, title
              ORDER BY avg_rating DESC, reviews DESC
            """, nativeQuery = true)
    List<Object[]> serviceRatings(@Param("from") Timestamp from, @Param("to") Timestamp to);

    @Query(value = """
              SELECT so.id, so.created_at, so.status, so.total,
                     so.vendor_id, COALESCE(vp.display_name,'(N/A)') vendor_name
              FROM service_order so
              LEFT JOIN vendor_profile vp ON vp.user_id=so.vendor_id
              WHERE so.created_at >= :from AND so.created_at < :to
                AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                AND (:status IS NULL OR so.status = :status)
              ORDER BY so.created_at DESC
              LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> orderList(@Param("from") Timestamp from, @Param("to") Timestamp to,
                             @Param("vendorId") Long vendorId,
                             @Param("status") String status,
                             @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
              SELECT COUNT(*) FROM service_order so
              WHERE so.created_at >= :from AND so.created_at < :to
                AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                AND (:status IS NULL OR so.status = :status)
            """, nativeQuery = true)
    long countOrders(@Param("from") Timestamp from, @Param("to") Timestamp to,
                     @Param("vendorId") Long vendorId,
                     @Param("status") String status);

    @Query(value = """
              SELECT so.id, so.created_at, so.status, so.total,
                     so.vendor_id, COALESCE(vp.display_name,'(N/A)') vendor_name,
                     COALESCE(SUM(CASE WHEN p.status='PAID' THEN p.amount END),0) paid_amount,
                     MAX(CASE WHEN p.status='PAID' THEN p.provider END) provider
              FROM service_order so
              LEFT JOIN vendor_profile vp ON vp.user_id = so.vendor_id
              LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id = so.id
              WHERE so.created_at >= :from AND so.created_at < :to
                AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                AND (:status IS NULL OR so.status = :status)
                AND (:provider IS NULL OR p.provider = :provider)
              GROUP BY so.id, so.created_at, so.status, so.total, so.vendor_id, vendor_name
              ORDER BY so.created_at DESC
              LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<Object[]> orderReport(@Param("from") Timestamp from, @Param("to") Timestamp to,
                               @Param("vendorId") Long vendorId, @Param("status") String status,
                               @Param("provider") String provider,
                               @Param("limit") int limit, @Param("offset") int offset);

    @Query(value = """
              SELECT COUNT(DISTINCT so.id)
              FROM service_order so
              LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id = so.id
              WHERE so.created_at >= :from AND so.created_at < :to
                AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                AND (:status IS NULL OR so.status = :status)
                AND (:provider IS NULL OR p.provider = :provider)
            """, nativeQuery = true)
    long countOrderReport(@Param("from") Timestamp from, @Param("to") Timestamp to,
                          @Param("vendorId") Long vendorId, @Param("status") String status,
                          @Param("provider") String provider);

    @Query(value = """
              SELECT COALESCE(SUM(so.total),0) total_order_amount,
                     COALESCE(SUM(CASE WHEN p.status='PAID' THEN p.amount END),0) total_paid,
                     COUNT(DISTINCT so.id) orders
              FROM service_order so
              LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id = so.id
              WHERE so.created_at >= :from AND so.created_at < :to
                AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                AND (:status IS NULL OR so.status = :status)
                AND (:provider IS NULL OR p.provider = :provider)
            """, nativeQuery = true)
    Object[] orderReportSummary(@Param("from") Timestamp from, @Param("to") Timestamp to,
                                @Param("vendorId") Long vendorId, @Param("status") String status,
                                @Param("provider") String provider);

    @Query(value = """
              SELECT so.id, so.created_at, so.status, so.total,
                     so.vendor_id, COALESCE(vp.display_name,'(N/A)') vendor_name,
                     COALESCE(SUM(CASE WHEN p.status='PAID' THEN p.amount END),0) paid_amount,
                     MAX(CASE WHEN p.status='PAID' THEN p.provider END) provider
              FROM service_order so
              LEFT JOIN vendor_profile vp ON vp.user_id = so.vendor_id
              LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id = so.id
              WHERE so.created_at >= :from AND so.created_at < :to
                AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                AND (:status  IS NULL OR so.status   = :status)
                AND (:provider IS NULL OR p.provider = :provider)
              GROUP BY so.id, so.created_at, so.status, so.total, so.vendor_id, vendor_name
              ORDER BY so.created_at DESC
            """,
            countQuery = """
                      SELECT COUNT(DISTINCT so.id)
                      FROM service_order so
                      LEFT JOIN payment p ON p.pay_target_type='SERVICE_ORDER' AND p.pay_target_id = so.id
                      WHERE so.created_at >= :from AND so.created_at < :to
                        AND (:vendorId IS NULL OR so.vendor_id = :vendorId)
                        AND (:status  IS NULL OR so.status   = :status)
                        AND (:provider IS NULL OR p.provider = :provider)
                    """, nativeQuery = true)
    Page<Object[]> orderReport(@Param("from") Timestamp from, @Param("to") Timestamp to,
                               @Param("vendorId") Long vendorId,
                               @Param("status") String status,
                               @Param("provider") String provider,
                               Pageable pageable);
}