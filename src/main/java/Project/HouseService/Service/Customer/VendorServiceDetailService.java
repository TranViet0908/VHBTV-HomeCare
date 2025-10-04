// src/main/java/Project/HouseService/Service/Customer/VendorServiceDetailService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.User;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Entity.VendorServiceMedia;
import Project.HouseService.Entity.VendorServiceReview;
import Project.HouseService.Repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VendorServiceDetailService {

    private final VendorServiceRepository vendorServiceRepo;
    private final ServiceRepository serviceRepo;
    private final UserRepository userRepo;
    private final VendorServiceMediaRepository mediaRepo;
    private final VendorServiceReviewRepository reviewRepo;
    private final ServiceOrderItemRepository orderItemRepo;
    private final VendorReviewRepository vendorReviewRepo; // NEW

    public VendorServiceDetailService(VendorServiceRepository vendorServiceRepo,
                                      ServiceRepository serviceRepo,
                                      UserRepository userRepo,
                                      VendorServiceMediaRepository mediaRepo,
                                      VendorServiceReviewRepository reviewRepo,
                                      ServiceOrderItemRepository orderItemRepo,
                                      VendorReviewRepository vendorReviewRepo) { // NEW
        this.vendorServiceRepo = vendorServiceRepo;
        this.serviceRepo = serviceRepo;
        this.userRepo = userRepo;
        this.mediaRepo = mediaRepo;
        this.reviewRepo = reviewRepo;
        this.orderItemRepo = orderItemRepo;
        this.vendorReviewRepo = vendorReviewRepo; // NEW
    }

    public Map<String, Object> loadDetail(Long vsId, Integer page, Integer size) {
        VendorService vs = vendorServiceRepo.findById(vsId)
                .orElseThrow(() -> new NoSuchElementException("VendorService not found"));
        if (!"ACTIVE".equalsIgnoreCase(vs.getStatus())) {
            throw new NoSuchElementException("VendorService is not active");
        }

        Project.HouseService.Entity.Service svc = serviceRepo.findById(vs.getServiceId())
                .orElseThrow(() -> new NoSuchElementException("Service not found"));
        User vendor = userRepo.findById(vs.getVendorId())
                .orElseThrow(() -> new NoSuchElementException("Vendor user not found"));

        // Media
        List<VendorServiceMedia> mediaList = mediaRepo.findByVendorService_IdOrderBySortOrderAscIdAsc(vsId);
        List<VendorServiceMedia> imageList = mediaList.stream()
                .filter(m -> m.getMediaType()!=null && "IMAGE".equalsIgnoreCase(m.getMediaType().name()))
                .collect(Collectors.toList());

        // Orders completed count
        long ordersCompleted;
        try {
            ordersCompleted = orderItemRepo.countCompletedItemsByVendorServiceId(vsId);
        } catch (Exception ignore) {
            ordersCompleted = vendorServiceRepo.countOrdersByVendorServiceIds(Set.of(vsId))
                    .stream()
                    .filter(m -> Objects.equals(((Number)m.get("vsId")).longValue(), vsId))
                    .map(m -> ((Number)m.get("cnt")).longValue())
                    .findFirst().orElse(0L);
        }

        // Reviews của gói hiện tại
        int p = (page==null||page<0)?0:page;
        int s = (size==null||size<=0||size>50)?10:size;
        Page<VendorServiceReview> reviewsPage =
                reviewRepo.findByVendorServiceIdAndHiddenFalseOrderByCreatedAtDesc(vsId, PageRequest.of(p, s));

        // Thống kê rating CHO GÓI DỊCH VỤ (vendor_service_id)
        long ratingCount = reviewRepo.countVisibleByVendorServiceId(vsId);
        Double avgObj = reviewRepo.avgByVendorServiceId(vsId);
        double avgRating = avgObj==null?0.0:avgObj.doubleValue();

        // Thống kê rating CHO VENDOR từ bảng vendor_review
        long vendorRatingCount = vendorReviewRepo.countVisibleByVendorId(vs.getVendorId());
        Double vendorAvgObj = vendorReviewRepo.avgByVendorIdVisible(vs.getVendorId());
        double vendorAvgRating = vendorAvgObj==null?0.0:vendorAvgObj.doubleValue();

        // Map customer -> tên hiển thị
        Set<Long> cids = reviewsPage.getContent().stream()
                .map(VendorServiceReview::getCustomerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long,String> customerNamesById = new HashMap<>();
        if (!cids.isEmpty()) userRepo.findAllById(cids).forEach(u -> customerNamesById.put(u.getId(), pickDisplayName(u)));

        // Lịch tối thiểu
        LocalDateTime minScheduleAt = LocalDateTime.now()
                .plusHours(vs.getMinNoticeHours()==null?0:vs.getMinNoticeHours());
        String minScheduleAtISO = minScheduleAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        boolean vendorVerified = Boolean.TRUE.equals(vendor.getActive());
        String vendorAvatarUrl = pickAvatarUrl(vendor);
        String vendorDisplayName = pickDisplayName(vendor); // dùng display_name

        Map<String,Object> model = new HashMap<>();
        model.put("vendorService", vs);
        model.put("service", svc);
        model.put("vendor", vendor);

        model.put("vendorVerified", vendorVerified);
        model.put("vendorAvatarUrl", vendorAvatarUrl);
        model.put("vendorDisplayName", vendorDisplayName); // NEW

        model.put("mediaList", mediaList);
        model.put("imageList", imageList);

        model.put("ordersCount", ordersCompleted);

        // Gói hiện tại
        model.put("avgRating", avgRating);
        model.put("ratingCount", ratingCount);

        // Vendor
        model.put("vendorAvgRating", vendorAvgRating);       // NEW
        model.put("vendorRatingCount", vendorRatingCount);   // NEW

        model.put("reviewsPage", reviewsPage);
        model.put("customerNamesById", customerNamesById);

        model.put("minScheduleAt", minScheduleAt);
        model.put("minScheduleAtISO", minScheduleAtISO);
        return model;
    }

    // Ưu tiên: fullName -> displayName -> name -> username
    private String pickDisplayName(User u) {
        String v = tryGetter(u,"getFullName");   if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getDisplayName");       if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getName");              if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getUsername");          if (isNonEmpty(v)) return v;
        return "Khách hàng";
    }
    private String pickAvatarUrl(User u) {
        String v = tryGetter(u,"getAvatar");     if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getAvatarUrl");         if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getImageUrl");          if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getProfileImage");      if (isNonEmpty(v)) return v;
        v = tryGetter(u,"getAvatarPath");        if (isNonEmpty(v)) return v;
        return null;
    }
    private boolean isNonEmpty(String s){ return s!=null && !s.trim().isEmpty(); }
    private String tryGetter(User u, String m){
        try { Method mm=u.getClass().getMethod(m); Object val=mm.invoke(u); return val!=null?val.toString():null; }
        catch(Exception ignored){ return null; }
    }
}
