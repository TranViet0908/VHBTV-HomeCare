// src/main/java/Project/HouseService/Service/Admin/CouponUserAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.Coupon;
import Project.HouseService.Entity.CouponUser;
import Project.HouseService.Entity.User;
import Project.HouseService.Repository.CouponUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CouponUserAdminService {
    private final CouponUserRepository repo;
    public CouponUserAdminService(CouponUserRepository repo){ this.repo = repo; }

    @Transactional(readOnly = true)
    public List<CouponUser> listByCoupon(Long couponId){ return repo.findByCoupon_Id(couponId); }

    public void add(Coupon coupon, User user){
        if (!repo.existsByCoupon_IdAndUser_Id(coupon.getId(), user.getId())) {
            CouponUser cu = new CouponUser();
            cu.setCoupon(coupon);
            cu.setUser(user);
            repo.save(cu);
        }
    }

    public void remove(Long couponId, Long userId){ repo.deleteByCoupon_IdAndUser_Id(couponId, userId); }
}
