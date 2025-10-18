package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.CouponRedemption;
import Project.HouseService.Entity.Payment;
import Project.HouseService.Entity.ServiceOrder;
import Project.HouseService.Repository.*;
import Project.HouseService.Service.Customer.PaymentService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import Project.HouseService.Entity.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
public class PaymentController {
    @PersistenceContext
    private EntityManager em;

    private final ServiceOrderItemRepository serviceOrderItemRepository;
    private final PaymentService paymentService;
    private final ServiceOrderRepository orders;
    private final PaymentRepository payments;
    private final CouponRepository coupons;
    private final CouponRedemptionRepository couponRedemptions;
    private final UserRepository users;

    public PaymentController(PaymentService paymentService,
                             ServiceOrderRepository orders,
                             PaymentRepository payments,
                             CouponRepository coupons,
                             CouponRedemptionRepository couponRedemptions,
                             UserRepository users,
                             ServiceOrderItemRepository serviceOrderItemRepository) {
        this.paymentService = paymentService;
        this.orders = orders;
        this.payments = payments;
        this.coupons = coupons;
        this.couponRedemptions = couponRedemptions;
        this.users = users;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
    }

    // Entry từ confirm
    @PostMapping("/customer/payment/vnpay/checkout")
    public String processCheckout(@RequestParam("paymentMethod") String paymentMethod,
                                  @RequestParam("orderId") Long orderId,
                                  HttpServletRequest request,
                                  Model model) {
        if (!StringUtils.hasText(paymentMethod)) {
            model.addAttribute("error", "Thiếu phương thức thanh toán");
            return "redirect:/customer/checkout/confirm";
        }
        String method = paymentMethod.trim().toUpperCase();
        if ("VNPAY".equals(method)) {
            Map<String, Object> m = paymentService.createVnpayCheckout(orderId, request);
            model.addAllAttributes(m);
            return "customer/payment/redirecting";
        }
        // COD hoặc khác → quay lại confirm
        return "redirect:/customer/checkout/confirm";
    }

    @GetMapping("/customer/payment/vnpay/checkout")
    public String vnpayCheckout(@RequestParam(value="orderId", required=false) Long orderId,
                                @RequestParam(value="orderIds", required=false) String orderIds,
                                HttpServletRequest request, Model model) {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        if (orderIds != null && !orderIds.isBlank()) {
            for (String s : orderIds.split(",")) if (!s.isBlank()) ids.add(Long.valueOf(s.trim()));
        } else if (orderId != null) {
            ids = java.util.List.of(orderId);
        } else {
            throw new IllegalArgumentException("Missing orderId/orderIds");
        }
        model.addAllAttributes(paymentService.createVnpayCheckoutForOrders(ids, request));
        return "customer/payment/redirecting";
    }

    @GetMapping("/customer/payment/vnpay/return")
    public String vnpayReturn(HttpServletRequest request, Model model) {
        boolean ok = paymentService.handleVnpReturn(request.getParameterMap());

        String txnRef = request.getParameter("vnp_TxnRef");
        Optional<Payment> p = (txnRef == null) ? Optional.empty() : payments.findByTransactionRef(txnRef);

        model.addAttribute("paymentStatus", ok ? "SUCCESS" : "FAILED");
        model.addAttribute("transactionRef", txnRef);
        model.addAttribute("amountDisplay", p.map(Payment::getAmount).map(BigDecimal::toPlainString).orElse(null));
        model.addAttribute("amount",        p.map(Payment::getAmount).orElse(null));      // thêm
        model.addAttribute("paidAt",        p.map(Payment::getPaidAt).orElse(null));      // thêm
        model.addAttribute("responseCode",  request.getParameter("vnp_ResponseCode"));
        model.addAttribute("bankCode",      request.getParameter("vnp_BankCode"));
        model.addAttribute("bankTransNo",   request.getParameter("vnp_BankTranNo"));
        model.addAttribute("message",       ok ? "Thanh toán thành công" : "Thanh toán thất bại");
        return "customer/payment/vnpay_return";
    }

    // IPN
    @PostMapping("/customer/payment/vnpay/ipn")
    @ResponseBody
    public Map<String, String> vnpayIpn(HttpServletRequest request) {
        return paymentService.handleVnpIpn(request.getParameterMap());
    }
}
