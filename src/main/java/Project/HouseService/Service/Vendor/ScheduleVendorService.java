// src/main/java/Project/HouseService/Service/Vendor/ScheduleVendorService.java
package Project.HouseService.Service.Vendor;

import Project.HouseService.Entity.ServiceOrderItem;
import Project.HouseService.Entity.VendorProfile;
import Project.HouseService.Repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service("scheduleVendorService")
public class ScheduleVendorService {

    private final VendorProfileRepository vendorProfileRepository;
    private final ServiceOrderItemRepository orderItemRepository;
    private final ServiceOrderRepository serviceOrderRepository;
    private final UserRepository userRepository;

    private static final ZoneId ZONE = ZoneId.of("Asia/Bangkok");

    public ScheduleVendorService(VendorProfileRepository vendorProfileRepository,
                                 ServiceOrderItemRepository orderItemRepository,
                                 ServiceOrderRepository serviceOrderRepository,
                                 UserRepository userRepository) {
        this.vendorProfileRepository = vendorProfileRepository;
        this.orderItemRepository = orderItemRepository;
        this.serviceOrderRepository = serviceOrderRepository;
        this.userRepository = userRepository;
    }

    public Long currentVendorId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        String username = auth.getName();
        return vendorProfileRepository.findByUser_Username(username)
                .map(VendorProfile::getId).orElse(null);
    }

    private Long currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        return userRepository.findByUsername(auth.getName())
                .map(u -> u.getId()).orElse(null);
    }

    public LocalDate today() { return LocalDate.now(ZONE); }

    // ===== Điều hướng tháng =====
    public LocalDate resolveBaseMonth(String ymParam) {
        try {
            if (ymParam != null && !ymParam.isBlank()) {
                var ym = YearMonth.parse(ymParam); // yyyy-MM
                return ym.atDay(1);
            }
        } catch (Exception ignored) {}
        LocalDate t = today();
        return LocalDate.of(t.getYear(), t.getMonth(), 1);
    }

    public String formatMonth(LocalDate firstOfMonth) {
        return YearMonth.from(firstOfMonth).toString(); // yyyy-MM
    }

    public LocalDate prevMonth(LocalDate firstOfMonth) {
        return YearMonth.from(firstOfMonth).minusMonths(1).atDay(1);
    }

    public LocalDate nextMonth(LocalDate firstOfMonth) {
        return YearMonth.from(firstOfMonth).plusMonths(1).atDay(1);
    }

    // ===== Lưới 6 tuần x 7 ngày cho tháng =====
    public LocalDate monthGridStart(LocalDate firstOfMonth) {
        // Bắt đầu từ thứ Hai gần nhất trước hoặc bằng ngày 01
        DayOfWeek dow = firstOfMonth.getDayOfWeek();
        int shift = dow.getValue() - DayOfWeek.MONDAY.getValue();
        if (shift < 0) shift += 7;
        return firstOfMonth.minusDays(shift);
    }

    public LocalDate monthGridEnd(LocalDate firstOfMonth) {
        return monthGridStart(firstOfMonth).plusDays(41); // 6 tuần * 7
    }

    public List<List<LocalDate>> monthGrid(LocalDate firstOfMonth) {
        LocalDate start = monthGridStart(firstOfMonth);
        return IntStream.range(0, 6) // 6 tuần
                .mapToObj(w -> IntStream.range(0, 7) // 7 ngày
                        .mapToObj(d -> start.plusDays(w * 7L + d))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    public boolean sameMonth(LocalDate baseFirstDay, LocalDate d) {
        return baseFirstDay.getMonthValue() == d.getMonthValue() && baseFirstDay.getYear() == d.getYear();
    }

    // ===== Đếm trạng thái đơn theo ngày (không dùng bảng mới) =====
    public Map<LocalDate, Map<String, Long>> statusCountsByDay(Long vendorProfileId,
                                                               LocalDate gridStart, LocalDate gridEnd) {
        if (vendorProfileId == null) return Collections.emptyMap();

        List<Long> vendorIds = new ArrayList<>();
        vendorIds.add(vendorProfileId);
        Long uid = currentUserId();
        if (uid != null) vendorIds.add(uid);

        LocalDateTime from = gridStart.atStartOfDay();
        LocalDateTime to = gridEnd.atTime(23, 59, 59);

        List<ServiceOrderItem> items =
                orderItemRepository.findByVendorIdInAndScheduledAtBetweenOrderByScheduledAtAsc(vendorIds, from, to);

        if (items.isEmpty()) return Collections.emptyMap();

        // Lấy map orderId -> status một lần
        List<Long> orderIds = items.stream().map(ServiceOrderItem::getServiceOrderId).distinct().toList();
        Map<Long, String> idToStatus = serviceOrderRepository.findIdAndStatusByIdIn(orderIds).stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> String.valueOf(r[1])
                ));

        Map<LocalDate, Map<String, Long>> out = new LinkedHashMap<>();
        for (ServiceOrderItem it : items) {
            if (it.getScheduledAt() == null) continue;
            LocalDate d = it.getScheduledAt().toLocalDate();
            String st = idToStatus.getOrDefault(it.getServiceOrderId(), "UNKNOWN");
            out.computeIfAbsent(d, k -> new LinkedHashMap<>())
                    .merge(st, 1L, Long::sum);
        }
        return out;
    }
}
