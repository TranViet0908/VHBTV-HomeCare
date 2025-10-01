// src/main/java/Project/HouseService/Controller/Vendor/ScheduleVendorController.java
package Project.HouseService.Controller.Vendor;

import Project.HouseService.Service.Vendor.ScheduleVendorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ScheduleVendorController {

    private final ScheduleVendorService service;

    public ScheduleVendorController(ScheduleVendorService service) {
        this.service = service;
    }

    @GetMapping("/vendor/schedule")
    public String calendarPage(Model model) {
        model.addAttribute("nav", "schedule");
        return "vendor/schedule/calendar";
    }
}
