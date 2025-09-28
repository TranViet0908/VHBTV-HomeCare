// src/main/java/Project/HouseService/Service/Admin/PaymentAdminService.java
package Project.HouseService.Service.Admin;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PaymentAdminService {

    private final PaymentRepository payments;

    public PaymentAdminService(PaymentRepository payments) {
        this.payments = payments;
    }

    // ========= Giữ nguyên phương thức cũ =========
    public List<Payment> listForOrder(Long orderId) {
        return payments.findByPayTargetTypeAndPayTargetId(Payment.PayTargetType.SERVICE_ORDER, orderId);
    }

    @Transactional
    public void markPaid(Long paymentId) {
        Payment p = payments.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment không tồn tại: " + paymentId));
        p.setStatus(Payment.PaymentStatus.PAID);
        if (p.getPaidAt() == null) p.setPaidAt(LocalDateTime.now());
        payments.save(p);
    }

    @Transactional
    public void markRefunded(Long paymentId) {
        Payment p = payments.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment không tồn tại: " + paymentId));
        p.setStatus(Payment.PaymentStatus.REFUNDED);
        payments.save(p);
    }
    // ============================================

    // ========= Bổ sung cho controller =========

    public Payment getOrThrow(Long id) {
        return payments.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment không tồn tại: " + id));
    }

    /**
     * Tìm kiếm có phân trang. Không thay đổi PaymentRepository.
     * Lấy toàn bộ rồi lọc trong bộ nhớ.
     */
    public Page<Payment> search(String q,
                                String status,
                                String provider,
                                String currency,
                                LocalDateTime from,
                                LocalDateTime to,
                                BigDecimal minAmount,
                                BigDecimal maxAmount,
                                Long serviceOrderId,
                                Pageable pageable) {

        List<Payment> all = baseList(serviceOrderId);

        String qLower = (q != null) ? q.trim().toLowerCase() : null;
        Payment.PaymentStatus statusEnum = parseEnum(status, Payment.PaymentStatus.class);
        Payment.Provider providerEnum = parseEnum(provider, Payment.Provider.class);
        // FIX: rỗng thì coi như không lọc
        String currencyUpper = (currency != null && !currency.trim().isEmpty())
                ? currency.trim().toUpperCase()
                : null;

        List<Payment> filtered = all.stream()
                .filter(p -> {
                    if (qLower != null && !qLower.isEmpty()) {
                        String txn = safe(p.getTransactionRef()).toLowerCase();
                        String idStr = String.valueOf(p.getId());
                        String targetId = String.valueOf(p.getPayTargetId());
                        if (!(txn.contains(qLower) || idStr.contains(qLower) || targetId.contains(qLower))) return false;
                    }
                    if (statusEnum != null && p.getStatus() != statusEnum) return false;
                    if (providerEnum != null && p.getProvider() != providerEnum) return false;
                    // FIX: chỉ so khi currencyUpper != null
                    if (currencyUpper != null && !currencyUpper.equalsIgnoreCase(safe(p.getCurrency()))) return false;

                    LocalDateTime pivot = p.getPaidAt();
                    if (pivot == null && p.getCreatedAt() != null) {
                        pivot = LocalDateTime.ofInstant(p.getCreatedAt(), java.time.ZoneId.systemDefault());
                    }
                    if (from != null && pivot != null && pivot.isBefore(from)) return false;
                    if (to != null && pivot != null && pivot.isAfter(to)) return false;

                    BigDecimal amt = p.getAmount() != null ? p.getAmount() : BigDecimal.ZERO;
                    if (minAmount != null && amt.compareTo(minAmount) < 0) return false;
                    if (maxAmount != null && amt.compareTo(maxAmount) > 0) return false;

                    if (serviceOrderId != null) {
                        if (p.getPayTargetType() != Payment.PayTargetType.SERVICE_ORDER) return false;
                        if (!Objects.equals(p.getPayTargetId(), serviceOrderId)) return false;
                    }
                    return true;
                })
                .sorted((a, b) -> {
                    LocalDateTime ta = a.getPaidAt();
                    LocalDateTime tb = b.getPaidAt();
                    LocalDateTime la = ta != null ? ta
                            : (a.getCreatedAt() != null ? LocalDateTime.ofInstant(a.getCreatedAt(), java.time.ZoneId.systemDefault()) : LocalDateTime.MIN);
                    LocalDateTime lb = tb != null ? tb
                            : (b.getCreatedAt() != null ? LocalDateTime.ofInstant(b.getCreatedAt(), java.time.ZoneId.systemDefault()) : LocalDateTime.MIN);
                    return lb.compareTo(la);
                })
                .collect(Collectors.toList());

        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();
        int total = filtered.size();
        int end = Math.min(offset + size, total);
        List<Payment> content = offset >= total ? Collections.emptyList() : filtered.subList(offset, end);

        return new PageImpl<>(content, pageable, total);
    }

    // ---- Đối soát: tổng quan
    public static class ReconcileSummary {
        public long countAll;
        public long countPaid;
        public long countPending;
        public long countProblem;
        public BigDecimal sumAll = BigDecimal.ZERO;
        public BigDecimal sumPaid = BigDecimal.ZERO;
        public BigDecimal sumPending = BigDecimal.ZERO;
        public BigDecimal sumProblem = BigDecimal.ZERO;

        public long getCountAll() { return countAll; }
        public long getCountPaid() { return countPaid; }
        public long getCountPending() { return countPending; }
        public long getCountProblem() { return countProblem; }
        public BigDecimal getSumAll() { return sumAll; }
        public BigDecimal getSumPaid() { return sumPaid; }
        public BigDecimal getSumPending() { return sumPending; }
        public BigDecimal getSumProblem() { return sumProblem; }
    }

    // ---- Đối soát: theo provider
    public static class ReconcileRow {
        public String provider;
        public long countAll;
        public BigDecimal sumAll = BigDecimal.ZERO;
        public BigDecimal sumPaid = BigDecimal.ZERO;
        public BigDecimal sumPending = BigDecimal.ZERO;
        public BigDecimal sumProblem = BigDecimal.ZERO;

        public String getProvider() { return provider; }
        public long getCountAll() { return countAll; }
        public BigDecimal getSumAll() { return sumAll; }
        public BigDecimal getSumPaid() { return sumPaid; }
        public BigDecimal getSumPending() { return sumPending; }
        public BigDecimal getSumProblem() { return sumProblem; }
    }

    public ReconcileSummary reconcileSummary(LocalDateTime from,
                                             LocalDateTime to,
                                             String provider,
                                             String currency) {
        List<Payment> list = searchBase(from, to, provider, currency);

        ReconcileSummary s = new ReconcileSummary();
        s.countAll = list.size();
        for (Payment p : list) {
            BigDecimal amt = nvl(p.getAmount());
            s.sumAll = s.sumAll.add(amt);
            if (p.getStatus() == Payment.PaymentStatus.PAID) {
                s.countPaid++;
                s.sumPaid = s.sumPaid.add(amt);
            } else if (p.getStatus() == Payment.PaymentStatus.PENDING) {
                s.countPending++;
                s.sumPending = s.sumPending.add(amt);
            } else if (p.getStatus() == Payment.PaymentStatus.FAILED
                    || p.getStatus() == Payment.PaymentStatus.REFUNDED) {
                s.countProblem++;
                s.sumProblem = s.sumProblem.add(amt);
            }
        }
        return s;
    }

    public List<ReconcileRow> reconcileByProvider(LocalDateTime from,
                                                  LocalDateTime to,
                                                  String provider,
                                                  String currency) {
        List<Payment> list = searchBase(from, to, provider, currency);

        Map<String, ReconcileRow> map = new LinkedHashMap<>();
        for (Payment p : list) {
            String key = p.getProvider() != null ? p.getProvider().name() : "UNKNOWN";
            ReconcileRow row = map.computeIfAbsent(key, k -> new ReconcileRow());
            row.provider = key;
            row.countAll++;
            BigDecimal amt = nvl(p.getAmount());
            row.sumAll = row.sumAll.add(amt);
            if (p.getStatus() == Payment.PaymentStatus.PAID) {
                row.sumPaid = row.sumPaid.add(amt);
            } else if (p.getStatus() == Payment.PaymentStatus.PENDING) {
                row.sumPending = row.sumPending.add(amt);
            } else if (p.getStatus() == Payment.PaymentStatus.FAILED
                    || p.getStatus() == Payment.PaymentStatus.REFUNDED) {
                row.sumProblem = row.sumProblem.add(amt);
            }
        }
        return new ArrayList<>(map.values());
    }

    // ==================== Helpers ====================

    private List<Payment> baseList(Long serviceOrderId) {
        if (serviceOrderId != null) {
            try {
                return payments.findByPayTargetTypeAndPayTargetId(Payment.PayTargetType.SERVICE_ORDER, serviceOrderId);
            } catch (Throwable ignored) {
                return payments.findAll(); // fallback
            }
        }
        return payments.findAll();
    }

    private List<Payment> searchBase(LocalDateTime from,
                                     LocalDateTime to,
                                     String provider,
                                     String currency) {
        Payment.Provider providerEnum = parseEnum(provider, Payment.Provider.class);
        String currencyUpper = currency != null ? currency.trim().toUpperCase() : null;

        return payments.findAll().stream()
                .filter(p -> {
                    if (providerEnum != null && p.getProvider() != providerEnum) return false;
                    if (currencyUpper != null && !currencyUpper.equalsIgnoreCase(safe(p.getCurrency()))) return false;

                    LocalDateTime pivot = p.getPaidAt();
                    if (pivot == null) {
                        Instant c = p.getCreatedAt();
                        pivot = (c != null) ? LocalDateTime.ofInstant(c, java.time.ZoneId.systemDefault()) : null;
                    }
                    if (from != null && pivot != null && pivot.isBefore(from)) return false;
                    if (to != null && pivot != null && pivot.isAfter(to)) return false;
                    return true;
                })
                .collect(Collectors.toList());
    }

    private static String safe(String s) { return s == null ? "" : s; }
    private static BigDecimal nvl(BigDecimal b) { return b == null ? BigDecimal.ZERO : b; }

    private static <E extends Enum<E>> E parseEnum(String v, Class<E> type) {
        if (v == null || v.isBlank()) return null;
        try {
            return Enum.valueOf(type, v.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
