// src/main/java/Project/HouseService/Service/Admin/VendorRatingRecalcService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Repository.VendorReviewRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VendorRatingRecalcService {
    private final VendorReviewRepository vendorReviewRepo;
    private final VendorProfileRepository vendorProfileRepo;

    public VendorRatingRecalcService(VendorReviewRepository vrr, VendorProfileRepository vpr) {
        this.vendorReviewRepo = vrr;
        this.vendorProfileRepo = vpr;
    }

    @Transactional
    public void recalcForVendor(Long vendorId) {
        Object[] row = vendorReviewRepo.avgAndCountForVendor(vendorId);

        BigDecimal avg = BigDecimal.ZERO;
        int count = 0;

        if (row != null) {
            avg = toBigDecimal(row[0]).setScale(2, RoundingMode.HALF_UP);
            count = toInt(row[1]);
        }

        final BigDecimal avgFinal = avg;   // make effectively final
        final int countFinal = count;      // make effectively final

        vendorProfileRepo.findById(vendorId).ifPresent(v -> {
            v.setRatingAvg(avgFinal);      // BigDecimal
            v.setRatingCount(countFinal);  // Integer trong entity OK
            vendorProfileRepo.save(v);
        });
    }

    private static BigDecimal toBigDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal bd) return bd;
        if (o instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return BigDecimal.ZERO;
    }

    private static int toInt(Object o) {
        return (o instanceof Number n) ? n.intValue() : 0;
    }
}
