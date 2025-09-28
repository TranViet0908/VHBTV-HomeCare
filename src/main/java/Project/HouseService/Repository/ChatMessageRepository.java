package Project.HouseService.Repository;

import Project.HouseService.Entity.ChatConversation;
import Project.HouseService.Entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByConversationOrderByCreatedAtAsc(ChatConversation conversation);

    List<ChatMessage> findTop100ByConversation_IdOrderByCreatedAtDesc(Long conversationId);

    List<ChatMessage> findByConversation_IdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

}
