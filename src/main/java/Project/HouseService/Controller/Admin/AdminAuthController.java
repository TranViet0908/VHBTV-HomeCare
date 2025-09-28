// src/main/java/Project/HouseService/Controller/Admin/AdminAuthController.java
package Project.HouseService.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @GetMapping("/login")
    public String loginPage() {
        return "admin/auth/login";
    }
}
