// src/main/java/Project/HouseService/Service/Admin/CouponRedemptionAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.CouponRedemption;
import Project.HouseService.Repository.CouponRedemptionRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CouponRedemptionAdminService {
    private final CouponRedemptionRepository repo;
    public CouponRedemptionAdminService(CouponRedemptionRepository repo){ this.repo = repo; }

    public Page<CouponRedemption> list(Long couponId, int page, int size){
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "redeemedAt"));
        if (couponId != null) return repo.findByCoupon_Id(couponId, pageable);
        return repo.findAll(pageable);
    }
}
