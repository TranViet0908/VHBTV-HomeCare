// src/main/java/Project/HouseService/Controller/Customer/VendorApplyPageController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Service.Customer.VendorApplicationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/vendor")
public class VendorApplyPageController {

    private final VendorApplicationService service;

    public VendorApplyPageController(VendorApplicationService service) {
        this.service = service;
    }

    @GetMapping("/apply")
    public String applyPage(Authentication auth, Model model, jakarta.servlet.http.HttpSession session) {
        Long uid = resolveUserId(auth, session);
        // Nếu đã có đơn PENDING thì chuyển sang trang kết quả với trạng thái exists
        if (uid != null) {
            VendorApplication a = service.myPending(uid);
            if (a != null) return "redirect:/customer/vendor/result?status=exists";
        }
        model.addAttribute("userId", uid);
        return "customer/vendor/apply";
    }

    // Xử lý submit từ form và redirect ra trang kết quả
    @PostMapping("/apply/submit")
    public String applySubmit(Authentication auth,
                              @RequestParam(name = "userId", required = false) Long formUserId,
                              @RequestParam String displayName,
                              @RequestParam String fullName,
                              @RequestParam(required = false) String email,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String address,
                              @RequestParam(required = false) String region,
                              @RequestParam(required = false) Integer experienceYears,
                              @RequestParam(required = false) String note,
                              jakarta.servlet.http.HttpSession session) {
        Long uid = formUserId != null ? formUserId : resolveUserId(auth, session);
        if (uid == null) return "redirect:/customer/vendor/result?status=err";

        try {
            service.apply(uid, displayName, fullName, email, phone, address, region, experienceYears, note);
            return "redirect:/customer/vendor/result?status=ok";
        } catch (IllegalStateException e) { // đã có PENDING
            return "redirect:/customer/vendor/result?status=exists";
        } catch (Exception e) {
            return "redirect:/customer/vendor/result?status=err";
        }
    }

    @GetMapping("/result")
    public String resultPage(@RequestParam(name = "status", required = false) String status, Model model) {
        model.addAttribute("status", status);
        return "customer/vendor/result";
    }

    private Long resolveUserId(Authentication auth, jakarta.servlet.http.HttpSession session) {
        Long uid = null;
        if (auth != null) {
            Object principal = auth.getPrincipal();
            try {
                var m = principal.getClass().getMethod("getId");
                Object v = m.invoke(principal);
                if (v != null) uid = Long.valueOf(v.toString());
            } catch (Exception ignore) {}
            if (uid == null) try { uid = Long.parseLong(auth.getName()); } catch (Exception ignore) {}
        }
        if (uid == null && session != null) {
            Object v = session.getAttribute("USER_ID");
            if (v != null) try { uid = Long.valueOf(v.toString()); } catch (Exception ignore) {}
        }
        return uid;
    }
}
