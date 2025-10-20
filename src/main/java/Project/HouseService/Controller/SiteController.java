// src/main/java/Project/HouseService/Controller/SiteController.java
package Project.HouseService.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SiteController {
    @GetMapping("/about")
    public String about() {
        return "about"; // templates/about.html
    }
}
