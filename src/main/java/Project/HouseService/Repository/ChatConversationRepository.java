package Project.HouseService.Repository;

import Project.HouseService.Entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findByUserId(Long userId);

    Optional<ChatConversation> findByConversationUrl(String conversationUrl);

    Optional<ChatConversation> findByExternalConversationId(String externalConversationId);
}
