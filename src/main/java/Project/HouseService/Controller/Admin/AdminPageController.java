// src/main/java/Project/HouseService/Controller/Admin/AdminPageController.java
package Project.HouseService.Controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    // GIỮ redirect duy nhất cho /admin
    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }
}
