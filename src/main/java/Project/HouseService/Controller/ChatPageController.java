package Project.HouseService.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/chat")
public class ChatPageController {

    // Hỗ trợ /customer/chat, /customer/chat/, /customer/chat/index
    @GetMapping({"", "/", "/index"})
    public String index() {
        return "customer/chatbot/index"; // templates/customer/chatbot/index.html
    }
}
