package Project.HouseService.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {
    @GetMapping("/user/chat")
    public String chatPage() {
        return "user/chat"; // templates/user/chat.html
    }
}
