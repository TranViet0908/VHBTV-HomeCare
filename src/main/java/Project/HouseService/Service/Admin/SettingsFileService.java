// src/main/java/Project/HouseService/Service/Admin/SettingsFileService.java
package Project.HouseService.Service.Admin;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SettingsFileService {

    @Value("${vhbtv.settings.path:./data/settings.json}")
    private String settingsPathStr;

    private final ObjectMapper om = new ObjectMapper();
    private Path settingsPath;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    // ===== API dùng trong view/controller =====
    public String get(String key, String def) { return cache.getOrDefault(key, def); }
    public boolean getBool(String key, boolean def) {
        String v = cache.get(key);
        return v == null ? def : v.equalsIgnoreCase("true") || v.equals("1");
    }
    public int getInt(String key, int def) {
        try { return Integer.parseInt(cache.getOrDefault(key, String.valueOf(def))); }
        catch (Exception e) { return def; }
    }

    public void set(String scope, String key, String value, String adminUser) {
        cache.put(scope + "." + key, value == null ? "" : value);
    }

    public synchronized void persist() {
        try {
            Files.createDirectories(settingsPath.getParent());
            // lưu theo dạng flat key: { "core.siteName": "VHBTV Homecare", ... }
            Map<String, String> ordered = new LinkedHashMap<>(cache); // giữ thứ tự ổn định
            Path tmp = settingsPath.resolveSibling(settingsPath.getFileName() + ".tmp");
            om.writerWithDefaultPrettyPrinter().writeValue(tmp.toFile(), ordered);
            try {
                Files.move(tmp, settingsPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tmp, settingsPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ghi settings thất bại: " + e.getMessage(), e);
        }
    }

    public void evictAndReload() { cache.clear(); loadFromDisk(); }

    // ===== lifecycle =====
    @PostConstruct
    public void init() {
        settingsPath = Paths.get(settingsPathStr).toAbsolutePath().normalize();
        putDefaults();
        loadFromDisk();
    }

    private void putDefaults() {
        Map<String, String> d = new LinkedHashMap<>();
        d.put("core.siteName","VHBTV Homecare");
        d.put("core.timezone","Asia/Ho_Chi_Minh");
        d.put("core.currency","VND");
        d.put("orders.minNoticeHours","2");
        d.put("orders.maxJobsPerDay","8");
        d.put("payments.cod.enabled","true");
        d.put("payments.vnpay.enabled","false");
        d.put("payments.momo.enabled","false");
        d.put("upload.maxSizeMb","10");
        d.put("review.minStarsVisible","3");
        d.put("security.maintenance","false");
        cache.putAll(d);
    }

    private void loadFromDisk() {
        if (!Files.exists(settingsPath)) return;
        try {
            Map<String, String> fileMap = om.readValue(settingsPath.toFile(), new TypeReference<>() {});
            if (fileMap != null) cache.putAll(fileMap);
        } catch (IOException e) {
            // Nếu file hỏng thì bỏ qua và giữ defaults
        }
    }
}
