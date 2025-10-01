package Project.HouseService.Service;

import Project.HouseService.Gateway.AiGateway;
import Project.HouseService.Entity.ChatConversation;
import Project.HouseService.Entity.ChatMessage;
import Project.HouseService.Repository.ChatConversationRepository;
import Project.HouseService.Repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class ChatService {

    private final ChatConversationRepository convRepo;
    private final ChatMessageRepository msgRepo;
    private final AiGateway ai;

    @Value("${chat.guest-user-id}")
    private Long guestUserId;

    public ChatService(ChatConversationRepository convRepo, ChatMessageRepository msgRepo, AiGateway ai) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.ai = ai;
    }

    private static final Pattern SENSITIVE = Pattern.compile(
            "(?i)(password|m[aă]t\\s*k[h]?a[uù]|otp|api\\s*key|token|bypass|hack|dump|database|c[ơo] s[ởo]\\s*d[ữu] li[ệe]u|schema|b[aả]ng\\s*user|email\\s*danh s[aá]ch|s[ốo]\\s*\\d{9,}|th[ẻe]\\s*visa|ccv|cvv)"
    );

    public record ChatResult(Long conversationId, String answer) { }

    public String greeting() { return ai.greeting(); }

    @Transactional
    public ChatResult chat(Long userId, Long conversationId, String question) {
        if (question == null || question.isBlank()) {
            return new ChatResult(conversationId, "Bạn vui lòng nhập câu hỏi cụ thể.");
        }
        if (SENSITIVE.matcher(question).find()) {
            return new ChatResult(conversationId,
                    "Xin lỗi, Sana không hỗ trợ yêu cầu liên quan đến thông tin nhạy cảm hoặc nội bộ. "
                            + "Mình có thể hướng dẫn về dịch vụ, giá tham khảo, hoặc cách tự tra cứu đơn trên website.");
        }

        boolean isGuest = userId == null || userId.equals(guestUserId);

        List<String[]> pairs = new ArrayList<>();
        String answer;
        try {
            answer = ai.generate(question, pairs);
        } catch (Exception e) {
            // Phòng khi gateway có lỗi bất thường
            answer = "Xin lỗi, hiện chưa thể phản hồi. Bạn thử lại sau hoặc vào mục Đơn hàng của tôi để tự tra cứu.";
        }

        if (isGuest) {
            return new ChatResult(null, answer); // không lưu
        }

        ChatConversation conv;
        if (conversationId == null) {
            conv = new ChatConversation();
            conv.setUserId(userId);
            conv.setTitle("Trao đổi với Sana");
            // KHÔNG gọi setConversationId(...) vì entity không có field này
            conv.setCreatedAt(Instant.now());
            conv.setUpdatedAt(Instant.now());
            conv = convRepo.save(conv);
            conversationId = conv.getId();
        } else {
            conv = convRepo.findById(conversationId).orElseThrow();
            conv.setUpdatedAt(Instant.now());
            convRepo.save(conv);
        }

        ChatMessage m = new ChatMessage();
        m.setUserId(userId);
        // Nếu ChatMessage dùng @ManyToOne ChatConversation conversation:
        m.setConversation(conv);
        // Nếu ChatMessage dùng Long conversationId thì thay dòng trên bằng:
        // m.setConversationId(conv.getId());

        m.setQuestion(question);
        m.setAnswer(answer);
        m.setCreatedAt(Instant.now());
        msgRepo.save(m);

        return new ChatResult(conversationId, answer);
    }
}
