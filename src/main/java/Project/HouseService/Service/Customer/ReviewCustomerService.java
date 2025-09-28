// src/main/java/Project/HouseService/Service/Customer/ReviewCustomerService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.VendorReview;
import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
public class ReviewCustomerService {

    private final VendorReviewRepository vendorReviewRepo;
    private final VendorServiceReviewRepository vendorServiceReviewRepo;
    private final ServiceOrderRepository orderRepo;
    private final ServiceOrderItemRepository itemRepo;

    public ReviewCustomerService(VendorReviewRepository vr,
                                 VendorServiceReviewRepository vsr,
                                 ServiceOrderRepository or,
                                 ServiceOrderItemRepository ir) {
        this.vendorReviewRepo = vr;
        this.vendorServiceReviewRepo = vsr;
        this.orderRepo = or;
        this.itemRepo = ir;
    }

    @Transactional
    public VendorReview createVendorReview(Long customerId, Long vendorId, Long serviceOrderId,
                                           Integer rating, String content) {
        if (!orderRepo.existsCompletedBetweenCustomerAndVendor(customerId, vendorId))
            throw new IllegalArgumentException("Bạn chưa hoàn tất đơn với vendor này");
        if (vendorReviewRepo.existsByServiceOrderId(serviceOrderId))
            throw new IllegalArgumentException("Đơn này đã có đánh giá");

        var rv = new VendorReview();
        rv.setCustomerId(customerId);
        rv.setVendorId(vendorId);
        rv.setServiceOrderId(serviceOrderId);
        rv.setRating(rating);
        rv.setContent(content);
        rv.setCreatedAt(LocalDateTime.now());
        return vendorReviewRepo.save(rv);
    }

    @Transactional
    public VendorServiceReview createVendorServiceReview(Long customerId, Long vendorId,
                                                         Long vendorServiceId, Long serviceOrderItemId,
                                                         Integer rating, String content) {
        if (!itemRepo.existsCompletedForCustomerAndItem(customerId, serviceOrderItemId))
            throw new IllegalArgumentException("Bạn chưa hoàn tất đơn với dịch vụ này");
        if (vendorServiceReviewRepo.existsByServiceOrderItemId(serviceOrderItemId))
            throw new IllegalArgumentException("Mục đơn này đã có đánh giá");

        var rv = new VendorServiceReview();
        rv.setCustomerId(customerId);
        rv.setVendorId(vendorId);
        rv.setVendorServiceId(vendorServiceId);
        rv.setServiceOrderItemId(serviceOrderItemId);
        rv.setRating(rating);
        rv.setContent(content);
        rv.setCreatedAt(LocalDateTime.now());
        return vendorServiceReviewRepo.save(rv);
    }
}
