package Project.HouseService.Controller;

import Project.HouseService.Entity.User;
import Project.HouseService.Repository.UserRepository;
import Project.HouseService.Service.ChatService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping(path = "/customer/chat", produces = MediaType.APPLICATION_JSON_VALUE)
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    public ChatController(ChatService chatService, UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @PostMapping(
            value = "/send",
            consumes = {
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE,
                    MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.TEXT_PLAIN_VALUE
            }
    )
    public ResponseEntity<?> send(Authentication auth,
                                  @RequestParam(required = false) MultiValueMap<String,String> form,
                                  @RequestBody(required = false) Map<String,Object> json,
                                  @RequestParam(required = false) String message) {

        Long userId = resolveUserId(auth);

        String msg = firstNonBlank(
                message,
                form == null ? null : form.getFirst("message"),
                form == null ? null : form.getFirst("q"),
                form == null ? null : form.getFirst("text"),
                form == null ? null : form.getFirst("content"),
                pickFromJson(json)
        );
        if (!StringUtils.hasText(msg)) return jsonError(HttpStatus.BAD_REQUEST, "EMPTY_MESSAGE");

        var reply = chatService.handleMessage(userId, msg);
        if (reply.getMeta().get("error") != null) {
            return jsonError(HttpStatus.BAD_GATEWAY, String.valueOf(reply.getMeta().get("error")));
        }
        Map<String,Object> ok = new HashMap<>();
        ok.put("ok", true);
        ok.put("reply", reply.getText());
        ok.put("meta", reply.getMeta());
        return ResponseEntity.ok(ok);
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(Authentication auth,
                                     @RequestParam(defaultValue = "20") int limit,
                                     @RequestParam(defaultValue = "0") int offset) {
        Long userId = resolveUserId(auth);
        return ResponseEntity.ok(chatService.loadHistory(userId, limit, offset));
    }

    // ===== helpers =====
    private Long resolveUserId(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return 999L;
        Object p = auth.getPrincipal();
        String username = (p instanceof UserDetails u) ? u.getUsername() : String.valueOf(p);
        if (!StringUtils.hasText(username) || "anonymousUser".equals(username)) return 999L;
        return userRepository.findByUsername(username).map(User::getId).orElse(999L);
    }

    private static String firstNonBlank(String... arr) {
        if (arr == null) return null;
        for (String s : arr) if (StringUtils.hasText(s)) return s;
        return null;
    }

    private static ResponseEntity<Map<String, Object>> jsonError(HttpStatus code, String err) {
        Map<String, Object> m = new HashMap<>();
        m.put("error", err == null ? "UNKNOWN" : err);
        return ResponseEntity.status(code).body(m);
    }

    @SuppressWarnings("unchecked")
    private static String pickFromJson(Map<String,Object> json) {
        if (json == null) return null;
        Object v = json.get("message");
        if (v == null) v = json.get("q");
        if (v == null) v = json.get("text");
        if (v == null) v = json.get("content");
        if (v == null) v = json.get("prompt");
        if (v != null && StringUtils.hasText(String.valueOf(v))) return String.valueOf(v);

        // Gemini schema
        Object contents = json.get("contents");
        if (contents instanceof Iterable<?> it) {
            for (Object o : it) {
                if (o instanceof Map<?, ?> m) {
                    Object parts = m.get("parts");
                    if (parts instanceof Iterable<?> it2) {
                        for (Object p : it2) {
                            if (p instanceof Map<?, ?> pm) {
                                Object t = pm.get("text");
                                if (t != null && StringUtils.hasText(String.valueOf(t))) {
                                    return String.valueOf(t);
                                }
                            }
                        }
                    }
                }
            }
        }
        // OpenAI-like
        Object arr = json.get("messages");
        if (arr instanceof Iterable<?> it3) {
            for (Object o : it3) {
                if (o instanceof Map<?,?> m && "user".equals(String.valueOf(m.get("role")))) {
                    Object t = m.get("content");
                    if (t != null && StringUtils.hasText(String.valueOf(t))) return String.valueOf(t);
                }
            }
        }
        return null;
    }
}
