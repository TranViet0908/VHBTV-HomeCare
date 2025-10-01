package Project.HouseService.Controller;

import Project.HouseService.Service.ChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/customer/chat")
public class ChatController {

    private final ChatService chat;

    public ChatController(ChatService chat) { this.chat = chat; }

    @GetMapping
    public String page(Model model, Authentication auth) {
        Long userId = auth == null ? null : extractUserId(auth);
        model.addAttribute("greeting", chat.greeting());
        model.addAttribute("conversationId", null);
        model.addAttribute("userId", userId == null ? 999L : userId);
        return "customer/chatbot/index";
    }

    @PostMapping("/send")
    @ResponseBody
    public String send(@RequestParam("q") String q,
                       @RequestParam(value = "conversationId", required = false) Long conversationId,
                       Authentication auth,
                       HttpServletResponse resp) {

        Long userId = auth == null ? 999L : extractUserId(auth);
        ChatService.ChatResult r = chat.chat(userId, conversationId, q);
        if (r.conversationId() != null) {
            resp.setHeader("X-Conversation-Id", String.valueOf(r.conversationId()));
        }
        String safe = escapeHtml(r.answer());
        return """
               <div class="flex items-start gap-3 p-3">
                 <div class="w-8 h-8 rounded-full bg-blue-600 text-white flex items-center justify-center">S</div>
                 <div class="bg-gray-100 rounded-2xl px-4 py-2 max-w-[80%%] whitespace-pre-wrap">%s</div>
               </div>
               """.formatted(safe);
    }

    private Long extractUserId(Authentication auth) {
        try { return Long.valueOf(auth.getName()); } catch (Exception e) { return null; }
    }
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
