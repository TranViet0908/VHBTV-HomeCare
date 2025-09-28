//// src/main/java/ProjectSpringboot/AdvertisingCompany/Controller/User/ChatController.java
//package Project.HouseService.Controller;
//
//import Project.HouseService.Entity.ChatConversation;
//import Project.HouseService.Entity.ChatMessage;
//import Project.HouseService.Service.Admin.UserService;
//import Project.HouseService.Service.ChatService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
//@RestController
//@RequestMapping("/api/chat")
//public class ChatController {
//
//    private final ChatService chatService;
//    private final UserService userService;
//    @Value("${chat.guest-user-id:999}")
//    private Long guestUserId;
//
//    public ChatController(ChatService chatService, UserService userService) {
//        this.chatService = chatService;
//        this.userService = userService;
//    }
//
//    // Gửi câu hỏi (tự lấy userId từ đăng nhập; khách → dùng guestUserId)
//    @PostMapping("/ask")
//    public ResponseEntity<ChatResp> ask(@RequestBody ChatReq req, Authentication auth) {
//        Long userId = resolveUserId(auth);
//        String answer = chatService.ask(userId, req.question());
//        return ResponseEntity.ok(new ChatResp(answer));
//    }
//
//    // Thông tin conversation hiện hành của user/guest
//    @GetMapping("/conversation")
//    public ResponseEntity<?> conversation(Authentication auth) {
//        Long userId = resolveUserId(auth);
//        Optional<ChatConversation> opt = chatService.getConversationOf(userId);
//        return opt.<ResponseEntity<?>>map(c -> ResponseEntity.ok(Map.of(
//                "id", c.getId(),
//                "title", c.getTitle(),
//                "conversationUrl", c.getConversationUrl(),           // có thể null với Ollama
//                "externalConversationId", c.getExternalConversationId() // có thể null
//        ))).orElseGet(() -> ResponseEntity.ok(Map.of("status", "EMPTY")));
//    }
//
//    // Lịch sử chat (trả mảng role/content/createdAt)
//    @GetMapping("/history")
//    public List<Map<String, Object>> history(@RequestParam(defaultValue = "50") int limit,
//                                             Authentication auth) {
//        Long userId = resolveUserId(auth);
//        List<ChatMessage> list = chatService.getHistoryEntities(userId, limit);
//        List<Map<String, Object>> out = new ArrayList<>(list.size() * 2);
//        for (ChatMessage m : list) {
//            out.add(Map.of("role", "user", "content", m.getQuestion(), "createdAt", m.getCreatedAt()));
//            if (m.getAnswer() != null && !m.getAnswer().isBlank()) {
//                out.add(Map.of("role", "assistant", "content", m.getAnswer(), "createdAt", m.getCreatedAt()));
//            }
//        }
//        return out;
//    }
//
//    private Long resolveUserId(Authentication auth) {
//        if (auth == null || !auth.isAuthenticated()
//                || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
//            return guestUserId;
//        }
//        var u = userService.findByUsername(auth.getName());
//        return (u != null && u.getId() != null) ? u.getId() : guestUserId;
//    }
//
//    public record ChatReq(String question) {
//    }
//
//    public record ChatResp(String answer) {
//    }
//}
