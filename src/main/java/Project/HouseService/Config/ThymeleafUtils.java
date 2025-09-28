package Project.HouseService.Config;

import Project.HouseService.Repository.ServiceOrderRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@Component("utils")
public class ThymeleafUtils {

    private final VendorProfileRepository vendorProfiles;
    private final VendorServiceRepository vendorServices;
    private final UserRepository users;
    private final ServiceOrderRepository serviceOrders;

    public ThymeleafUtils(VendorProfileRepository vendorProfiles,
                          VendorServiceRepository vendorServices,
                          ServiceOrderRepository serviceOrders,
                          UserRepository users) {
        this.vendorProfiles = vendorProfiles;
        this.vendorServices = vendorServices;
        this.serviceOrders = serviceOrders;
        this.users = users;
    }
    // ==== tên từ vendor_profile.id ====
    public String vendorNameByProfileId(Long vendorProfileId) {
        if (vendorProfileId == null) return "N/A";
        return vendorProfiles.findById(vendorProfileId)
                .map(v -> {
                    String n = (v.getDisplayName() != null && !v.getDisplayName().isBlank())
                            ? v.getDisplayName() : v.getLegalName();
                    return (n == null || n.isBlank()) ? ("Vendor #" + vendorProfileId) : n;
                })
                .orElse("Vendor #" + vendorProfileId);
    }

    // ==== tên từ vendor_service.id → vendor_profile.id ====
    public String vendorNameByService(Long vendorServiceId) {
        if (vendorServiceId == null) return "N/A";
        return vendorServices.findById(vendorServiceId)
                .map(s -> {
                    try {
                        // ưu tiên getVendorProfileId(), fallback getVendorId()
                        Long pid = null;
                        try { pid = (Long) s.getClass().getMethod("getVendorProfileId").invoke(s); }
                        catch (NoSuchMethodException ignored) {
                            Object vId = s.getClass().getMethod("getVendorId").invoke(s);
                            if (vId instanceof Number n) pid = n.longValue();
                        }
                        return vendorNameByProfileId(pid);
                    } catch (Exception e) { return "Vendor ?"; }
                })
                .orElse("Vendor ?");
    }

    // ==== tên từ service_order.id → vendor_profile.id ====
    public String vendorNameByOrder(Long serviceOrderId) {
        if (serviceOrderId == null) return "N/A";
        return serviceOrders.findById(serviceOrderId)
                .map(o -> {
                    try {
                        Long pid = null;
                        try { pid = (Long) o.getClass().getMethod("getVendorProfileId").invoke(o); }
                        catch (NoSuchMethodException ignored) {
                            Object vId = o.getClass().getMethod("getVendorId").invoke(o);
                            if (vId instanceof Number n) pid = n.longValue();
                        }
                        return vendorNameByProfileId(pid);
                    } catch (Exception e) { return "Vendor ?"; }
                })
                .orElse("Vendor ?");
    }
    public String formatCurrency(Double number) {
        if (number == null) return "0 ₫";

        // Tạo DecimalFormatSymbols cho Việt Nam
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.'); // Dấu chấm phân cách hàng nghìn
        symbols.setDecimalSeparator(',');  // Dấu phẩy phân cách thập phân

        // Tạo formatter với symbols tùy chỉnh
        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);

        return formatter.format(number) + " ₫";
    }

    public String formatCurrencyWithDecimal(Double number) {
        if (number == null) return "0,00 ₫";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter = new DecimalFormat("#,##0.00", symbols);

        return formatter.format(number) + " ₫";
    }

    // Format số thường (không có ký hiệu tiền tệ)
    public String formatNumber(Double number) {
        if (number == null) return "0";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');

        DecimalFormat formatter = new DecimalFormat("#,##0", symbols);

        return formatter.format(number);
    }

    // Format phần trăm
    public String formatPercent(Double number) {
        if (number == null) return "0%";

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setDecimalSeparator(',');

        DecimalFormat formatter = new DecimalFormat("#0.##", symbols);

        return formatter.format(number) + "%";
    }
    // === TRA TÊN HIỂN THỊ ===
    public String vendorName(Long vendorId) {
        if (vendorId == null) return "N/A";
        return vendorProfiles.findById(vendorId)
                .map(v -> {
                    String n = (v.getDisplayName() != null && !v.getDisplayName().isBlank())
                            ? v.getDisplayName()
                            : v.getLegalName();
                    return (n == null || n.isBlank()) ? ("Vendor #" + vendorId) : n;
                })
                .orElse("Vendor #" + vendorId);
    }

    public String vendorServiceName(Long vendorServiceId) {
        if (vendorServiceId == null) return "N/A";
        return vendorServices.findById(vendorServiceId)
                .map(s -> {
                    try {
                        var m = s.getClass().getMethod("getTitle");
                        Object t = m.invoke(s);
                        return t != null && !t.toString().isBlank() ? t.toString() : ("Service #" + vendorServiceId);
                    } catch (Exception ignore) { return "Service #" + vendorServiceId; }
                })
                .orElse("Service #" + vendorServiceId);
    }

    public String userName(Long userId) {
        if (userId == null) return "N/A";
        return users.findById(userId)
                .map(u -> {
                    try {
                        var m = u.getClass().getMethod("getUsername");
                        Object t = m.invoke(u);
                        return t != null && !t.toString().isBlank() ? t.toString() : ("User #" + userId);
                    } catch (Exception ignore) { return "User #" + userId; }
                })
                .orElse("User #" + userId);
    }
}