package Project.HouseService.Service;

import Project.HouseService.Gateway.AiGateway;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
public class ChatService {

    public static class Reply {
        private final String text;
        private final Map<String, Object> meta;
        public Reply(String text, Map<String, Object> meta) {
            this.text = text;
            this.meta = meta == null ? Collections.emptyMap() : meta;
        }
        public String getText() { return text; }
        public Map<String, Object> getMeta() { return meta; }
    }

    private final AiGateway ai;

    public ChatService(AiGateway ai) { this.ai = ai; }

    public Reply handleMessage(Long userId, String content) {
        if (!StringUtils.hasText(content)) {
            return new Reply("", Map.of("error","EMPTY_MESSAGE"));
        }

        String answer;
        try {
            // có thể kèm historyPairs nếu cần
            answer = ai.generate(content, Collections.emptyList());
        } catch (Exception ex) {
            return new Reply("Xin lỗi, hiện chưa thể phản hồi. Vui lòng thử lại.",
                    Map.of("error","AI_UPSTREAM_ERROR"));
        }
        if (!StringUtils.hasText(answer)) {
            answer = "Xin lỗi, tôi chưa có câu trả lời phù hợp.";
        }

        // Không lưu khi khách vãng lai
        boolean guest = Objects.equals(userId, 999L);
        if (!guest) {
            // TODO: lưu ChatConversation/ChatMessage vào DB nếu bạn muốn
        }
        return new Reply(answer, Map.of("guest", guest));
    }

    public Object loadHistory(Long userId, int limit, int offset) {
        if (Objects.equals(userId, 999L)) return Collections.emptyList();
        // TODO: truy vấn DB theo userId
        return Collections.emptyList();
    }
}
