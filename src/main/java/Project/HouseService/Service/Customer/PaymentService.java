package Project.HouseService.Service.Customer;

import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Repository.PaymentRepository;
import Project.HouseService.Repository.ServiceOrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class PaymentService {

    private final PaymentRepository payments;
    private final ServiceOrderRepository orders;

    public PaymentService(PaymentRepository payments, ServiceOrderRepository orders) {
        this.payments = payments;
        this.orders = orders;
    }

    @Value("${vnpay.tmnCode}")
    private String vnpTmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnpHashSecret;

    @Value("${vnpay.payUrl}")
    private String vnpPayUrl;

    @Value("${vnpay.returnUrl}")
    private String vnpReturnUrl;

    @Value("${vnpay.ipnUrl:}")
    private String vnpIpnUrl;

    @Value("${vnpay.version:2.1.0}")
    private String vnpVersion;

    @Value("${vnpay.command:pay}")
    private String vnpCommand;

    @Value("${vnpay.currency:VND}")
    private String vnpCurrency;

    @Value("${vnpay.locale:vn}")
    private String vnpLocale;

    @Value("${vnpay.expireMinutes:15}")
    private int vnpExpireMinutes;

    @Value("${vnpay.orderType:other}")
    private String vnpOrderType;

    private static final DateTimeFormatter VNP_TS = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static String clientIp(HttpServletRequest req) {
        String ip = optHeader(req, "X-Forwarded-For");
        if (ip != null) {
            int c = ip.indexOf(',');
            return c > 0 ? ip.substring(0, c).trim() : ip.trim();
        }
        ip = optHeader(req, "X-Real-IP");
        if (ip != null) return ip.trim();
        return req.getRemoteAddr();
    }

    private static String optHeader(HttpServletRequest req, String name) {
        String v = req.getHeader(name);
        if (v == null || v.isBlank()) return null;
        return v;
    }

    public static String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec sk = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            mac.init(sk);
            byte[] result = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(result.length * 2);
            for (byte b : result) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failed", e);
        }
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.US_ASCII);
    }

    private static String buildQueryString(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            sb.append(urlEncode(e.getKey())).append('=').append(urlEncode(e.getValue()));
            if (it.hasNext()) sb.append('&');
        }
        return sb.toString();
    }

    private static String signData(Map<String, String> params, String secret) {
        String data = buildQueryString(params);
        return hmacSHA512(secret, data);
    }

    public static Map<String, String> filterAndSort(Map<String, String[]> requestParams) {
        SortedMap<String, String> out = new TreeMap<>();
        for (Map.Entry<String, String[]> e : requestParams.entrySet()) {
            String k = e.getKey();
            if (k == null) continue;
            if (k.equalsIgnoreCase("vnp_SecureHash") || k.equalsIgnoreCase("vnp_SecureHashType")) continue;
            String[] vals = e.getValue();
            if (vals == null || vals.length == 0) continue;
            String v = vals[0];
            if (v == null) v = "";
            out.put(k, v);
        }
        return out;
    }

    private static String toVnpAmount(java.math.BigDecimal amount) {
        if (amount == null) amount = java.math.BigDecimal.ZERO;
        // VNPay: VND * 100, dạng số nguyên
        return amount.multiply(java.math.BigDecimal.valueOf(100))
                .setScale(0, java.math.RoundingMode.DOWN)
                .toPlainString();
    }

    private static String sanitizeTxnRef(String ref) {
        if (ref == null) ref = "";
        // chỉ giữ A-Z a-z 0-9, _, -
        String cleaned = ref.replaceAll("[^A-Za-z0-9_-]", "");
        if (cleaned.length() < 8) cleaned = (cleaned + "XXXXXXXX").substring(0, 8);
        if (cleaned.length() > 32) cleaned = cleaned.substring(0, 32);
        return cleaned;
    }

    private static String newTxnRef() {
        String ts = LocalDateTime.now().format(VNP_TS);
        String rand = String.valueOf((long) (Math.random() * 1_000_000L));
        String ref = ts + rand;
        if (ref.length() > 32) ref = ref.substring(0, 32);
        return ref;
    }

    private static String normalizeIp(String ip) {
        if (ip == null || ip.isBlank()) return "127.0.0.1";
        // nếu là IPv6 thì trả IPv4 loopback
        return ip.contains(":") ? "127.0.0.1" : ip;
    }

    private static String buildReturnUrlWithIds(String base, Long orderId, Long paymentId, String txnRef) {
        StringBuilder ru = new StringBuilder(base);
        ru.append(base.contains("?") ? '&' : '?');
        ru.append("orderId=").append(orderId);
        ru.append("&paymentId=").append(paymentId);
        ru.append("&txnRef=").append(java.net.URLEncoder.encode(
                txnRef == null ? "" : txnRef, java.nio.charset.StandardCharsets.UTF_8));
        return ru.toString();
    }

    @Transactional
    public Map<String,Object> createVnpayCheckout(Long orderId, HttpServletRequest req) {
        return createVnpayCheckoutForOrders(java.util.List.of(orderId), req);
    }

    @Transactional
    public Map<String,Object> createVnpayCheckoutForOrders(java.util.List<Long> orderIds, HttpServletRequest req) {
        java.util.List<ServiceOrder> list = orders.findAllById(orderIds);
        if (list.isEmpty()) throw new IllegalArgumentException("Orders not found: " + orderIds);

        // tổng số tiền = tổng các đơn
        java.math.BigDecimal amount = list.stream()
                .map(o -> o.getTotal()==null? java.math.BigDecimal.ZERO : o.getTotal())
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        ServiceOrder first = list.get(0);

        Payment p = new Payment();
        p.setUserId(first.getCustomerId());
        p.setPayTargetType(Payment.PayTargetType.SERVICE_ORDER); // vẫn trỏ đơn đầu để tương thích
        p.setPayTargetId(first.getId());
        p.setProvider(Payment.Provider.VNPAY);
        p.setCurrency("VND");
        p.setAmount(amount);
        p.setStatus(Payment.PaymentStatus.PENDING);
        p.setTransactionRef(newTxnRef());
        payments.save(p);

        String create = java.time.LocalDateTime.now().format(VNP_TS);
        String expire = java.time.LocalDateTime.now().plusMinutes(vnpExpireMinutes).format(VNP_TS);

        String csv = orderIds.stream().map(String::valueOf).collect(java.util.stream.Collectors.joining(","));
        String returnUrl = appendQuery(vnpReturnUrl, java.util.Map.of(
                "orderIds", csv,
                "paymentId", String.valueOf(p.getId()),
                "txnRef", urlEncode(p.getTransactionRef())
        ));

        java.util.SortedMap<String,String> params = new java.util.TreeMap<>();
        params.put("vnp_Version",   "2.1.0");
        params.put("vnp_Command",   "pay");
        params.put("vnp_TmnCode",   vnpTmnCode);
        params.put("vnp_Amount",    toVnpAmount(amount));                // VND * 100
        params.put("vnp_CurrCode",  "VND");
        params.put("vnp_TxnRef",    sanitizeTxnRef(p.getTransactionRef()));
        params.put("vnp_OrderInfo", "Thanh toan " + list.size() + " don: " + first.getOrderCode());
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale",    "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        if (vnpIpnUrl != null && !vnpIpnUrl.isBlank() && !vnpIpnUrl.contains("localhost"))
            params.put("vnp_IpnUrl", vnpIpnUrl);
        params.put("vnp_IpAddr",    normalizeIp(clientIp(req)));
        params.put("vnp_CreateDate",create);
        params.put("vnp_ExpireDate",expire);

        // ký trên bộ tham số KHÔNG gồm vnp_SecureHashType
        String secureHash = signData(params, vnpHashSecret);

        java.util.Map<String,String> postParams = new java.util.LinkedHashMap<>(params);
        postParams.put("vnp_SecureHashType", "HmacSHA512"); // thêm sau khi ký
        postParams.put("vnp_SecureHash", secureHash);

        String paymentGetUrl = vnpPayUrl + "?" + buildQueryString(postParams);

        java.util.Map<String,Object> model = new java.util.HashMap<>();
        model.put("paymentUrl",    vnpPayUrl);
        model.put("paymentParams", postParams);
        model.put("paymentGetUrl", paymentGetUrl); // dùng GET redirect
        model.put("transactionRef", p.getTransactionRef());
        model.put("orderIds",      csv);
        model.put("paymentId",     p.getId());
        return model;
    }

    @Transactional
    public boolean handleVnpReturn(Map<String, String[]> requestParams) {
        SortedMap<String, String> sorted = new TreeMap<>();
        String givenHash = null;

        for (Map.Entry<String, String[]> e : requestParams.entrySet()) {
            String k = e.getKey();
            String[] vals = e.getValue();
            if (k == null) continue;

            // chỉ nhận key vnp_*
            if (!k.startsWith("vnp_")) continue;

            if (k.equalsIgnoreCase("vnp_SecureHash") || k.equalsIgnoreCase("vnp_SecureHashType")) {
                if (vals != null && vals.length > 0) givenHash = vals[0];
                continue;
            }
            if (vals == null || vals.length == 0) continue;
            sorted.put(k, vals[0]);
        }
        if (givenHash == null) return false;

        String calcHash = signData(sorted, vnpHashSecret);
        boolean ok = givenHash.equalsIgnoreCase(calcHash);
        String resp = sorted.getOrDefault("vnp_ResponseCode", "");

        String txnRef = sorted.getOrDefault("vnp_TxnRef", "");
        Optional<Payment> opt = payments.findByTransactionRef(txnRef);
        if (opt.isEmpty()) return false;

        Payment p = opt.get();

        String a = sorted.getOrDefault("vnp_Amount", "0");
        String expected = toVnpAmount(p.getAmount());
        if (!Objects.equals(a, expected)) ok = false;

        if (p.getStatus() != Payment.PaymentStatus.PAID) {
            p.setStatus(ok && "00".equals(resp) ? Payment.PaymentStatus.PAID : Payment.PaymentStatus.FAILED);
            if (p.getStatus() == Payment.PaymentStatus.PAID) p.setPaidAt(LocalDateTime.now());
            payments.save(p);
        }
        return ok && "00".equals(resp);
    }

    // helpers
    private static String appendQuery(String base, java.util.Map<String,String> q) {
        StringBuilder sb = new StringBuilder(base);
        char sep = base.contains("?") ? '&' : '?';
        for (var e : q.entrySet()) { sb.append(sep).append(e.getKey()).append('=').append(e.getValue()); sep='&'; }
        return sb.toString();
    }

    @Transactional
    public Map<String, String> handleVnpIpn(Map<String, String[]> requestParams) {
        SortedMap<String, String> sorted = new TreeMap<>();
        String givenHash = null;
        for (Map.Entry<String, String[]> e : requestParams.entrySet()) {
            String k = e.getKey();
            String[] vals = e.getValue();
            if (k == null || !k.startsWith("vnp_")) continue;
            if ("vnp_SecureHash".equalsIgnoreCase(k) || "vnp_SecureHashType".equalsIgnoreCase(k)) {
                if (vals != null && vals.length > 0) givenHash = vals[0];
                continue;
            }
            if (vals == null || vals.length == 0) continue;
            sorted.put(k, vals[0]);
        }

        Map<String, String> res = new LinkedHashMap<>();
        if (givenHash == null) {
            res.put("RspCode", "97");
            res.put("Message", "Missing signature");
            return res;
        }

        String calcHash = signData(sorted, vnpHashSecret);
        if (!givenHash.equalsIgnoreCase(calcHash)) {
            res.put("RspCode", "97");
            res.put("Message", "Invalid signature");
            return res;
        }

        String txnRef = sorted.getOrDefault("vnp_TxnRef", "");
        Optional<Payment> opt = payments.findByTransactionRef(txnRef);
        if (opt.isEmpty()) {
            res.put("RspCode", "01");
            res.put("Message", "Order not found");
            return res;
        }

        Payment p = opt.get();
        String a = sorted.getOrDefault("vnp_Amount", "0");
        String expected = toVnpAmount(p.getAmount());
        if (!Objects.equals(a, expected)) {
            res.put("RspCode", "04");
            res.put("Message", "Invalid amount");
            return res;
        }

        if (p.getStatus() == Payment.PaymentStatus.PAID) {
            res.put("RspCode", "02");
            res.put("Message", "Order already confirmed");
            return res;
        }

        String txnStatus = sorted.getOrDefault("vnp_TransactionStatus", "");
        if ("00".equals(txnStatus)) {
            p.setStatus(Payment.PaymentStatus.PAID);
            p.setPaidAt(LocalDateTime.now());
            payments.save(p);
            res.put("RspCode", "00");
            res.put("Message", "Confirm Success");
        } else {
            p.setStatus(Payment.PaymentStatus.FAILED);
            payments.save(p);
            res.put("RspCode", "00");
            res.put("Message", "Confirm Failed");
        }
        return res;
    }
}
