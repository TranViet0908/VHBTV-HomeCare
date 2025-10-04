// src/main/java/Project/HouseService/Service/Customer/ServicesPage/ServicesIndexService.java
package Project.HouseService.Service.Customer;

import Project.HouseService.Repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ServicesIndexService {

    private final ServiceRepository serviceRepo;
    private final VendorServiceRepository vendorServiceRepo;
    private final VendorServiceReviewRepository reviewRepo;
    private final ServiceOrderItemRepository orderItemRepo;
    private final CustomerEventRepository eventRepo;

    public ServicesIndexService(ServiceRepository serviceRepo,
                                VendorServiceRepository vendorServiceRepo,
                                VendorServiceReviewRepository reviewRepo,
                                ServiceOrderItemRepository orderItemRepo,
                                CustomerEventRepository eventRepo) {
        this.serviceRepo = serviceRepo;
        this.vendorServiceRepo = vendorServiceRepo;
        this.reviewRepo = reviewRepo;
        this.orderItemRepo = orderItemRepo;
        this.eventRepo = eventRepo;
    }

    // ===== Danh mục gốc + con =====
    public List<Project.HouseService.Entity.Service> listRoot(String q) {
        if (q != null && !q.trim().isEmpty()) {
            return serviceRepo.findRootByNameLike("%" + q.trim().toLowerCase() + "%");
        }
        return serviceRepo.findByParentIdIsNullOrderByNameAsc();
    }

    public List<Project.HouseService.Entity.Service> listChildren(Long parentId, String q) {
        if (q != null && !q.trim().isEmpty()) {
            return serviceRepo.findChildrenByParentIdAndNameLike(parentId, "%" + q.trim().toLowerCase() + "%");
        }
        return serviceRepo.findByParentIdOrderByNameAsc(parentId);
    }

    // ===== Enrich chỉ số hiển thị card =====
    public Map<Long, Map<String, Object>> enrichByServiceIds(Collection<Long> serviceIds) {
        Map<Long, Map<String, Object>> agg = new HashMap<>();
        if (serviceIds == null || serviceIds.isEmpty()) return agg;

        for (Long id : serviceIds) {
            Map<String, Object> m = new HashMap<>();
            m.put("vendorActiveCount", 0L);
            m.put("minPrice", null);
            m.put("ratingAvg", 0.0);
            m.put("ratingCount", 0L);
            m.put("avgDuration", null);
            m.put("minNotice", null);
            m.put("orders180d", 0L);
            agg.put(id, m);
        }

        vendorServiceRepo.countActiveByServiceIds(serviceIds).forEach(r -> {
            Long sid = ((Number) r.get("service_id")).longValue();
            Long cnt = ((Number) r.get("cnt")).longValue();
            agg.get(sid).put("vendorActiveCount", cnt);
        });

        vendorServiceRepo.minPriceByServiceIds(serviceIds).forEach(r -> {
            Long sid = ((Number) r.get("service_id")).longValue();
            BigDecimal min = (BigDecimal) r.get("min_price");
            agg.get(sid).put("minPrice", min);
        });

        vendorServiceRepo.durationAndNoticeByServiceIds(serviceIds).forEach(r -> {
            Long sid = ((Number) r.get("service_id")).longValue();
            Number avgDur = (Number) r.get("avg_duration");
            Number minNotice = (Number) r.get("min_notice");
            agg.get(sid).put("avgDuration", avgDur == null ? null : avgDur.intValue());
            agg.get(sid).put("minNotice", minNotice == null ? null : minNotice.intValue());
        });

        reviewRepo.ratingAggByServiceIds(serviceIds).forEach(r -> {
            Long sid = ((Number) r.get("service_id")).longValue();
            Double avg = r.get("avg_rating") == null ? 0.0 : ((Number) r.get("avg_rating")).doubleValue();
            Long cnt = ((Number) r.get("rating_count")).longValue();
            agg.get(sid).put("ratingAvg", avg);
            agg.get(sid).put("ratingCount", cnt);
        });

        LocalDateTime since180 = LocalDateTime.now().minusDays(180);
        orderItemRepo.completedCountByServiceIdsSince(serviceIds, since180).forEach(r -> {
            Long sid = ((Number) r.get("service_id")).longValue();
            Long cnt = ((Number) r.get("cnt")).longValue();
            agg.get(sid).put("orders180d", cnt);
        });

        return agg;
    }

    // ===== Icon đơn giản =====
    public String iconFor(Project.HouseService.Entity.Service s) {
        String slug = s.getSlug() == null ? "" : s.getSlug();
        if (slug.contains("ve-sinh")) return "fa-broom";
        if (slug.contains("dieu-hoa")) return "fa-fan";
        if (slug.contains("sofa")) return "fa-couch";
        if (slug.contains("may-giat")) return "fa-tshirt";
        if (slug.contains("son")) return "fa-paint-roller";
        if (slug.contains("dien")) return "fa-bolt";
        if (slug.contains("nuoc") || slug.contains("ong")) return "fa-faucet";
        return "fa-star";
    }

    // ===== Map entity -> card model =====
    public List<Map<String, Object>> toCards(List<Project.HouseService.Entity.Service> services,
                                             Map<Long, Map<String, Object>> enrich) {
        return services.stream().map(s -> {
            Map<String, Object> e = enrich.getOrDefault(s.getId(), Collections.emptyMap());
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("slug", s.getSlug());
            m.put("unit", s.getUnit());
            m.put("description", s.getDescription());
            m.put("icon", iconFor(s));
            m.put("vendorActiveCount", e.getOrDefault("vendorActiveCount", 0L));
            m.put("minPrice", e.get("minPrice"));
            m.put("ratingAvg", e.getOrDefault("ratingAvg", 0.0));
            m.put("ratingCount", e.getOrDefault("ratingCount", 0L));
            m.put("avgDuration", e.get("avgDuration"));
            m.put("minNotice", e.get("minNotice"));
            m.put("orders180d", e.getOrDefault("orders180d", 0L));
            return m;
        }).collect(Collectors.toList());
    }

    // ===== Gợi ý cho bạn =====
    public List<Map<String, Object>> suggestions(Long customerId, int limit) {
        if (customerId == null) return List.of();

        List<Long> serviceIds = new ArrayList<>();
        Map<Long, Double> score = new HashMap<>();

        LocalDateTime since30 = LocalDateTime.now().minusDays(30);
        double lambda = 0.08d;

        eventRepo.scoreByVendorServiceEvents(customerId, since30, lambda).forEach(row -> {
            Long sid = ((Number) row[0]).longValue();
            Double sc = ((Number) row[1]).doubleValue();
            score.merge(sid, sc, Double::sum);
        });
        eventRepo.scoreByServiceEvents(customerId, since30, lambda).forEach(row -> {
            Long sid = ((Number) row[0]).longValue();
            Double sc = ((Number) row[1]).doubleValue();
            score.merge(sid, sc, Double::sum);
        });

        serviceIds.addAll(
                score.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(limit)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList())   // <— thay cho .toList()
        );

        if (serviceIds.isEmpty()) {
            LocalDateTime since180 = LocalDateTime.now().minusDays(180);
            orderItemRepo.topTrendingServicesSince(since180, limit).forEach(r ->
                    serviceIds.add(((Number) r.get("service_id")).longValue())
            );
        }

        if (serviceIds.isEmpty()) return List.of();

        var services = serviceRepo.findByIdInOrderByNameAsc(serviceIds);
        var enrich = enrichByServiceIds(
                services.stream().map(Project.HouseService.Entity.Service::getId).collect(Collectors.toList())
        );

        Map<Long, Integer> idx = new HashMap<>();
        for (int i = 0; i < serviceIds.size(); i++) idx.put(serviceIds.get(i), i);
        services.sort(Comparator.comparingInt(s -> idx.getOrDefault(s.getId(), Integer.MAX_VALUE)));

        return toCards(services, enrich);
    }
}
