// src/main/java/ProjectSpringboot/AdvertisingCompany/Service/User/ChatService.java
package Project.HouseService.Service;

import Project.HouseService.Entity.ChatConversation;
import Project.HouseService.Entity.ChatMessage;
import Project.HouseService.Gateway.AiGateway;
import Project.HouseService.Repository.ChatConversationRepository;
import Project.HouseService.Repository.ChatMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);
    private static final long MIN_INTERVAL_MS = 3000;

    private final ChatConversationRepository convRepo;
    private final ChatMessageRepository msgRepo;
    private final AiGateway aiGateway;
    private final ConcurrentHashMap<Long, ReentrantLock> userLocks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastAskAt = new ConcurrentHashMap<>();
    // cấu hình lời chào
    @Value("${company.name:5PLUS ONLINE}")
    private String companyName;
    @Value("${chat.agent-name:CSKH}")
    private String agentName;
    @Value("${chat.greeting-template:Chào bạn! Mình là %s, nhân viên CSKH của công ty %s. Rất vui được hỗ trợ bạn!}")
    private String greetingTpl;

    public ChatService(ChatConversationRepository convRepo, ChatMessageRepository msgRepo, AiGateway aiGateway) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.aiGateway = aiGateway;
    }

    private ReentrantLock lockOf(Long userId) {
        return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
    }

    private void checkRateLimit(Long userId) {
        long now = System.currentTimeMillis();
        Long prev = lastAskAt.put(userId, now);
        if (prev != null && now - prev < MIN_INTERVAL_MS) {
            throw new IllegalArgumentException("Hỏi quá nhanh. Vui lòng thử lại sau vài giây.");
        }
    }

    /**
     * Nhận câu hỏi, gọi AI (Gemini), lưu DB, trả câu trả lời.
     */
    // ChatService.ask(...) – phiên bản đã sửa
    public String ask(Long userId, String question) {
        if (userId == null || question == null || question.isBlank())
            throw new IllegalArgumentException("userId/question rỗng");
        checkRateLimit(userId);

        ReentrantLock lock = lockOf(userId);
        boolean acquired = false;
        try {
            acquired = lock.tryLock(90, TimeUnit.SECONDS);
            if (!acquired) throw new RuntimeException("User đang bận. Thử lại sau.");

            // Ép tên agent & công ty, cấm tự đặt tên/giới thiệu lại khi đã có lời chào phía trên
            String systemPrompt = (
                    "Bạn là nhân viên CSKH cho công ty %s. Tên agent cố định: %s. " +
                            "Trả lời ngắn gọn, thân thiện, chuyên nghiệp. " +
                            "KHÔNG tự đặt tên khác hay công ty khác. " +
                            "Nếu tin nhắn đã có lời chào phía trên, KHÔNG tự chào/giới thiệu lại."
            ).formatted(companyName, agentName);

            // Lấy hoặc tạo conversation
            Optional<ChatConversation> existing = convRepo.findByUserId(userId);
            boolean isNew = existing.isEmpty();
            ChatConversation conv = existing.orElseGet(() -> {
                ChatConversation c = new ChatConversation();
                c.setUserId(userId);
                c.setTitle("[" + userId + "]");
                return saveConversation(c);
            });

            String greeting = String.format(greetingTpl, agentName, companyName);
            String answer = aiGateway.ask(systemPrompt, question);

            // CHỈ chèn lời chào khi cuộc chat mới
            if (isNew) {
                answer = greeting + "\n" + answer;
            }

            saveMessage(conv, userId, question, answer);
            return answer;

        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted");
        } finally {
            if (acquired) lock.unlock();
        }
    }

    @Transactional
    protected ChatConversation saveConversation(ChatConversation conv) {
        return convRepo.save(conv);
    }

    @Transactional
    protected void saveMessage(ChatConversation conv, Long userId, String question, String answer) {
        ChatMessage m = new ChatMessage();
        m.setConversation(conv);
        m.setUserId(userId);
        m.setQuestion(question);
        m.setAnswer(answer);
        msgRepo.save(m);
    }

    /**
     * Lấy conversation đang dùng của user (một user một conversation).
     */
    public Optional<ChatConversation> getConversationOf(Long userId) {
        return convRepo.findByUserId(userId);
    }

    /**
     * Lấy lịch sử theo thời gian tăng dần.
     */
    public List<ChatMessage> getHistoryEntities(Long userId, int limit) {
        Optional<ChatConversation> convOpt = convRepo.findByUserId(userId);
        if (convOpt.isEmpty()) return List.of();
        ChatConversation conv = convOpt.get();

        int lim = Math.max(1, Math.min(limit, 200));
        List<ChatMessage> rows = msgRepo.findByConversation_IdOrderByCreatedAtDesc(conv.getId(), PageRequest.of(0, lim));
        Collections.reverse(rows);
        return rows;
    }
}
