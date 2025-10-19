package Project.HouseService.Config;

import Project.HouseService.Repository.ServiceOrderRepository;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Repository.VendorProfileRepository;
import Project.HouseService.Repository.VendorServiceRepository;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public String vendorSlug(String username, String displayName) {
        if (username != null && !username.isBlank()) return username; // giữ nguyên username để khớp DB
        return toSlug(displayName);
    }

    public String toSlug(String input) {
        if (input == null) return "";
        String n = java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        n = n.replaceAll("[^\\p{Alnum}]+", "-")
                .replaceAll("^-+|-+$", "")
                .replaceAll("-{2,}", "-")
                .toLowerCase(java.util.Locale.ROOT);
        return n;
    }
    // ====== VIDEO URL HELPERS (YouTube / Google Drive) ======

    // Regex cố định cho YouTube: watch?v=, youtu.be/, shorts/, embed/
    private static final Pattern YT_WATCH = Pattern.compile("[?&]v=([A-Za-z0-9_-]{6,})");
    private static final Pattern YT_SHORT = Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{6,})");
    private static final Pattern YT_SHORTS = Pattern.compile("youtube\\.com/shorts/([A-Za-z0-9_-]{6,})");
    private static final Pattern YT_EMBED = Pattern.compile("youtube\\.com/embed/([A-Za-z0-9_-]{6,})");

    // Regex cho Google Drive: file/d/{id}, open?id=, uc?id=
    private static final Pattern GD_FILE_D = Pattern.compile("drive\\.google\\.com/(?:file/)?d/([A-Za-z0-9_-]+)");
    private static final Pattern GD_OPEN_ID = Pattern.compile("[?&]id=([A-Za-z0-9_-]+)");

    /** true nếu là URL YouTube hợp lệ */
    public boolean isYoutubeUrl(String url) {
        if (url == null) return false;
        String u = url.toLowerCase(Locale.ROOT);
        if (!(u.contains("youtube.com") || u.contains("youtu.be"))) return false;
        return extractYoutubeId(url) != null;
    }

    /** true nếu là URL Google Drive khả năng preview */
    public boolean isDriveUrl(String url) {
        if (url == null) return false;
        String u = url.toLowerCase(Locale.ROOT);
        if (!u.contains("drive.google.com")) return false;
        return extractDriveId(url) != null;
    }

    /** Trả về link embed YouTube: https://www.youtube.com/embed/{id} hoặc chuỗi rỗng nếu không trích xuất được */
    public String youtubeEmbed(String url) {
        String id = extractYoutubeId(url);
        return (id == null) ? "" : ("https://www.youtube.com/embed/" + id);
    }

    /** Trả về link preview Drive: https://drive.google.com/file/d/{id}/preview hoặc chuỗi rỗng nếu không trích xuất được */
    public String drivePreview(String url) {
        String id = extractDriveId(url);
        return (id == null) ? "" : ("https://drive.google.com/file/d/" + id + "/preview");
    }

    /**
     * Chuẩn hóa URL phát video:
     *  - YouTube  → trả về embed
     *  - Drive    → trả về preview
     *  - Khác     → trả nguyên URL
     */
    public String normalizeVideoUrl(String url) {
        if (url == null || url.isBlank()) return "";
        if (isYoutubeUrl(url)) return youtubeEmbed(url);
        if (isDriveUrl(url)) return drivePreview(url);
        return url;
    }

    /**
     * true nếu link nên render bằng <iframe> (YouTube/Drive),
     * false nếu render <video> nguồn trực tiếp (.mp4/.webm/…)
     */
    public boolean isIframePreferred(String url) {
        return isYoutubeUrl(url) || isDriveUrl(url);
    }

    // ---- Private helpers ----

    private String extractYoutubeId(String url) {
        if (url == null) return null;
        Matcher m1 = YT_WATCH.matcher(url);
        if (m1.find()) return m1.group(1);
        Matcher m2 = YT_SHORT.matcher(url);
        if (m2.find()) return m2.group(1);
        Matcher m3 = YT_SHORTS.matcher(url);
        if (m3.find()) return m3.group(1);
        Matcher m4 = YT_EMBED.matcher(url);
        if (m4.find()) return m4.group(1);
        return null;
    }

    private String extractDriveId(String url) {
        if (url == null) return null;
        Matcher m1 = GD_FILE_D.matcher(url);
        if (m1.find()) return m1.group(1);
        if (url.contains("drive.google.com")) {
            Matcher m2 = GD_OPEN_ID.matcher(url);
            if (m2.find()) return m2.group(1);
        }
        return null;
    }
}