// src/main/java/Project/HouseService/Controller/Admin/SettingsAdminController.java
package Project.HouseService.Controller.Admin;

import Project.HouseService.Service.Admin.SettingsFileService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/admin/settings")
public class SettingsAdminController {

    private final SettingsFileService settings;

    public SettingsAdminController(SettingsFileService settings) { this.settings = settings; }

    @GetMapping
    public String index(@RequestParam(value = "tab", defaultValue = "core") String tab, Model model) {
        model.addAttribute("tab", tab);
        model.addAttribute("get", settings);
        return "admin/settings/index";
    }

    @PostMapping("/save/{scope}")
    public String saveScope(@PathVariable String scope,
                            @RequestParam Map<String, String> params,
                            Authentication auth) {
        String admin = auth != null ? auth.getName() : "admin";
        // xử lý checkbox: nếu có danh sách các checkbox gửi kèm _bool thì set false khi thiếu
        params.forEach((k, v) -> {
            if (k.startsWith("_")) return;
            settings.set(scope, k, v, admin);
        });
        // các khóa boolean được liệt kê tại _boolKeys, phân tách bởi dấu phẩy
        String boolKeys = params.get("_boolKeys"); // ví dụ: "cod.enabled,vnpay.enabled,momo.enabled,maintenance"
        if (boolKeys != null) {
            for (String k : boolKeys.split(",")) {
                String name = k.trim();
                if (name.isEmpty()) continue;
                if (!params.containsKey(name)) settings.set(scope, name, "false", admin);
            }
        }
        settings.persist();
        return "redirect:/admin/settings?tab=" + scope + "&saved=1";
    }

    @PostMapping("/cache/evict")
    public String evict() {
        settings.evictAndReload();
        return "redirect:/admin/settings?tab=core&cache=cleared";
    }
}
