// src/main/java/Project/HouseService/Controller/Customer/VendorApplicationController.java
package Project.HouseService.Controller.Customer;

import Project.HouseService.Entity.VendorApplication;
import Project.HouseService.Service.Customer.VendorApplicationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customer/vendor")
public class VendorApplicationController {

    private final VendorApplicationService service;

    public VendorApplicationController(VendorApplicationService service) {
        this.service = service;
    }

    private Long resolveUserId(Authentication auth,
                               Long headerUserId,
                               Long formUserId,
                               jakarta.servlet.http.HttpSession session) {
        if (formUserId != null) return formUserId;
        if (auth != null) {
            Object principal = auth.getPrincipal();
            try {
                var m = principal.getClass().getMethod("getId");
                Object v = m.invoke(principal);
                if (v != null) return Long.valueOf(v.toString());
            } catch (Exception ignore) {}
            try { return Long.parseLong(auth.getName()); } catch (Exception ignore) {}
        }
        if (session != null) {
            Object v = session.getAttribute("USER_ID");
            if (v != null) try { return Long.valueOf(v.toString()); } catch (Exception ignore) {}
        }
        if (headerUserId != null) return headerUserId;
        return null;
    }

    @GetMapping("/status")
    public ResponseEntity<?> myPending(Authentication auth,
                                       @RequestHeader(name = "X-USER-ID", required = false) Long headerUserId,
                                       @RequestParam(name = "userId", required = false) Long formUserId,
                                       jakarta.servlet.http.HttpSession session) {
        Long uid = resolveUserId(auth, headerUserId, formUserId, session);
        if (uid == null) return ResponseEntity.badRequest().body("Thiếu userId");
        VendorApplication a = service.myPending(uid);
        return a == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(a);
    }

    @PostMapping("/apply")
    public ResponseEntity<?> apply(Authentication auth,
                                   @RequestHeader(name = "X-USER-ID", required = false) Long headerUserId,
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
        Long uid = resolveUserId(auth, headerUserId, formUserId, session);
        if (uid == null) return ResponseEntity.badRequest().body("Thiếu userId");
        VendorApplication saved = service.apply(uid, displayName, fullName, email, phone, address, region, experienceYears, note);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(Authentication auth,
                                       @RequestHeader(name = "X-USER-ID", required = false) Long headerUserId,
                                       @RequestParam(name = "userId", required = false) Long formUserId,
                                       @PathVariable Long id,
                                       jakarta.servlet.http.HttpSession session) {
        Long uid = resolveUserId(auth, headerUserId, formUserId, session);
        if (uid == null) return ResponseEntity.badRequest().build();
        service.cancel(uid, id);
        return ResponseEntity.noContent().build();
    }
}
