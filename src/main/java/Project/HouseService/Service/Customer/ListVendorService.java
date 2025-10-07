// src/main/java/Project/HouseService/Service/Customer/ListVendorService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Entity.VendorService;
import Project.HouseService.Repository.CustomerEventRepository;
import Project.HouseService.Repository.ServiceOrderItemRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorReviewRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ListVendorService {

    private final VendorProfileRepository vendorProfileRepo;
    private final VendorServiceRepository vendorServiceRepo;
    private final VendorReviewRepository vendorReviewRepo;
    private final ServiceOrderItemRepository orderItemRepo;
    private final CustomerEventRepository eventRepo;

    public ListVendorService(VendorProfileRepository vendorProfileRepo,
                             VendorServiceRepository vendorServiceRepo,
                             VendorReviewRepository vendorReviewRepo,
                             ServiceOrderItemRepository orderItemRepo,
                             CustomerEventRepository eventRepo) {
        this.vendorProfileRepo = vendorProfileRepo;
        this.vendorServiceRepo = vendorServiceRepo;
        this.vendorReviewRepo = vendorReviewRepo;
        this.orderItemRepo = orderItemRepo;
        this.eventRepo = eventRepo;
    }

    public Map<String, Object> facets() {
        Map<String, Object> f = new HashMap<>();
        java.util.List<Object[]> rows = vendorServiceRepo.globalPriceRangeActive();
        BigDecimal min = BigDecimal.ZERO, max = BigDecimal.ZERO;
        if (rows != null && !rows.isEmpty()) {
            Object[] r = rows.get(0);
            min = asDecimal(r[0]);
            max = asDecimal(r[1]);
        }
        Map<String, Object> price = new HashMap<>();
        price.put("min", min);
        price.put("max", max);
        f.put("price", price);
        return f;
    }

    public Page<Map<String, Object>> searchVendors(
            String q,
            Boolean verified,
            BigDecimal ratingMin,
            Integer yearsMin,
            BigDecimal priceMin,
            BigDecimal priceMax,
            Integer durationMax,
            Integer noticeMax,
            String sort,
            int page,
            int size,
            HttpServletRequest request
    ) {
        Pageable unpaged = PageRequest.of(0, Math.max(size * 5, 100));
        Page<VendorProfile> base = vendorProfileRepo.search(
                StringUtils.hasText(q) ? q.trim() : null,
                verified,
                ratingMin,
                yearsMin,
                unpaged
        );

        List<VendorProfile> candidates = new ArrayList<>(base.getContent());

        if (priceMin != null || priceMax != null || durationMax != null || noticeMax != null) {
            List<Long> vendorIdsAll = candidates.stream().map(vp -> vp.getUser().getId()).toList();
            Set<Long> okVendors = new HashSet<>(vendorServiceRepo.filterVendorsByServiceConstraints(
                    vendorIdsAll, priceMin, priceMax, durationMax, noticeMax
            ));
            candidates = candidates.stream()
                    .filter(vp -> okVendors.contains(vp.getUser().getId()))
                    .collect(Collectors.toList());
        }

        List<Long> vendorIds = candidates.stream().map(vp -> vp.getUser().getId()).toList();

        // ==== BULK số liệu từ DB ====
        Map<Long, Long> completedOrders = fetchCompletedOrdersSafe(vendorIds);
        Map<Long, Instant> lastClicks = fetchLastClicks(vendorIds, request);
        Map<Long, Double> ratingAvg = new HashMap<>();
        Map<Long, Integer> ratingCnt = new HashMap<>();
        // Lấy rating & count từ bảng review để đúng DB
        List<Object[]> ratingRows = vendorReviewRepo.aggRatingByVendorIds(vendorIds);
        for (Object[] r : ratingRows) {
            Long vid = ((Number) r[0]).longValue();
            Double avg = r[1] == null ? 0d : ((Number) r[1]).doubleValue();
            Integer cnt = r[2] == null ? 0 : ((Number) r[2]).intValue();
            ratingAvg.put(vid, avg);
            ratingCnt.put(vid, cnt);
        }
        // Fallback nếu vendor chưa có review
        for (VendorProfile vp : candidates) {
            Long vid = vp.getUser().getId();
            ratingAvg.putIfAbsent(vid, vp.getRatingAvg() == null ? 0d : vp.getRatingAvg().doubleValue());
            ratingCnt.putIfAbsent(vid, vp.getRatingCount() == null ? 0 : vp.getRatingCount());
        }

        Map<Long, List<VendorService>> sampleServices = sampleServices(vendorIds, 3);

        long seed = tieSeed(request);
        Random rnd = new Random(seed);

        List<Map<String, Object>> cards = new ArrayList<>();
        for (VendorProfile vp : candidates) {
            Long vid = vp.getUser().getId();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("vendorId", vid);
            m.put("displayName", vp.getDisplayName());
            m.put("avatarUrl", vp.getUser() != null ? vp.getUser().getAvatarUrl() : null);
            m.put("verified", vp.getVerified());
            m.put("ratingAvg", ratingAvg.getOrDefault(vid, 0d));
            m.put("ratingCount", ratingCnt.getOrDefault(vid, 0));
            m.put("completedOrders", completedOrders.getOrDefault(vid, 0L));
            m.put("lastClickAt", lastClicks.get(vid));

            List<VendorService> sv = sampleServices.getOrDefault(vid, List.of());
            List<Map<String, Object>> svOut = sv.stream().map(x -> {
                Map<String, Object> z = new HashMap<>();
                z.put("serviceId", x.getServiceId());
                z.put("title", x.getTitle());
                z.put("priceFrom", x.getBasePrice());
                z.put("durationMinutes", x.getDurationMinutes());
                return z;
            }).collect(Collectors.toList());
            m.put("servicesSample", svOut);

            m.put("_rand", rnd.nextInt(1_000_000));
            cards.add(m);
        }

        // Sort: đơn ↓, rating ↓, click ↓, random
        cards.sort((a, b) -> {
            int c1 = Long.compare(((Number) b.get("completedOrders")).longValue(),
                    ((Number) a.get("completedOrders")).longValue());
            if (c1 != 0) return c1;
            int c2 = Double.compare(((Number) b.get("ratingAvg")).doubleValue(),
                    ((Number) a.get("ratingAvg")).doubleValue());
            if (c2 != 0) return c2;
            Instant la = (Instant) a.get("lastClickAt");
            Instant lb = (Instant) b.get("lastClickAt");
            int c3 = Comparator.nullsLast(Comparator.<Instant>naturalOrder()).reversed().compare(la, lb);
            if (c3 != 0) return c3;
            return Integer.compare((Integer) a.get("_rand"), (Integer) b.get("_rand"));
        });

        int from = Math.min(page * size, cards.size());
        int to = Math.min(from + size, cards.size());
        List<Map<String, Object>> pageItems = cards.subList(from, to);
        return new PageImpl<>(pageItems, PageRequest.of(page, size), cards.size());
    }

    public List<Map<String, Object>> suggestedVendors(HttpServletRequest request, int limit) {
        String sessionId = request.getSession(true).getId();
        LocalDateTime since = LocalDateTime.now().minusDays(14);
        var rows = eventRepo.recentClickedVendorsWithLastClick(sessionId, since, PageRequest.of(0, limit));
        List<Long> ids = rows.stream().map(r -> ((Number) r[0]).longValue()).toList();

        List<Map<String, Object>> out = new ArrayList<>();
        if (!ids.isEmpty()) {
            List<VendorProfile> vps = vendorProfileRepo.findByUserIdIn(ids);
            Map<Long, VendorProfile> map = vps.stream()
                    .collect(Collectors.toMap(v -> v.getUser().getId(), x -> x, (a, b) -> a));
            for (Long id : ids) {
                VendorProfile vp = map.get(id);
                if (vp == null) continue;
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("vendorId", vp.getUser().getId());
                m.put("displayName", vp.getDisplayName());
                m.put("avatarUrl", vp.getUser() != null ? vp.getUser().getAvatarUrl() : null);
                m.put("verified", vp.getVerified());
                m.put("ratingAvg", vp.getRatingAvg());
                m.put("ratingCount", vp.getRatingCount());
                out.add(m);
            }
        }
        return out;
    }

    // ===== helpers =====
    private Map<Long, Long> fetchCompletedOrdersSafe(List<Long> vendorIds) {
        if (vendorIds.isEmpty()) return Map.of();
        List<Object[]> rows = orderItemRepo.countCompletedByVendorIdsSafe(vendorIds);
        Map<Long, Long> map = new HashMap<>();
        for (Object[] r : rows) {
            Long vid = ((Number) r[0]).longValue();
            Long cnt = ((Number) r[1]).longValue();
            map.put(vid, cnt);
        }
        return map;
    }

    private Map<Long, Instant> fetchLastClicks(List<Long> vendorIds, HttpServletRequest request) {
        if (vendorIds.isEmpty()) return Map.of();
        String sessionId = request.getSession(true).getId();
        List<Object[]> rows = eventRepo.lastClicksForVendors(sessionId, vendorIds);
        Map<Long, Instant> map = new HashMap<>();
        for (Object[] r : rows) {
            Long vid = ((Number) r[0]).longValue();
            java.sql.Timestamp ts = (java.sql.Timestamp) r[1];
            map.put(vid, ts.toInstant());
        }
        return map;
    }

    private Map<Long, List<VendorService>> sampleServices(List<Long> vendorIds, int k) {
        List<VendorService> all = vendorServiceRepo.findActiveByVendorIdInOrderByPrice(vendorIds);
        Map<Long, List<VendorService>> grouped = new LinkedHashMap<>();
        for (VendorService vs : all) {
            grouped.computeIfAbsent(vs.getVendorId(), x -> new ArrayList<>());
            List<VendorService> arr = grouped.get(vs.getVendorId());
            if (arr.size() < k) arr.add(vs);
        }
        return grouped;
    }

    private long tieSeed(HttpServletRequest request) {
        String sessionId = request.getSession(true).getId();
        String basis = (sessionId == null ? "anon" : sessionId) + ":" + java.time.LocalDate.now();
        return basis.hashCode();
    }

    private BigDecimal asDecimal(Object o) {
        if (o == null) return BigDecimal.ZERO;
        if (o instanceof BigDecimal) return (BigDecimal) o;
        if (o instanceof Number) return BigDecimal.valueOf(((Number) o).doubleValue());
        return new BigDecimal(o.toString());
    }
}
