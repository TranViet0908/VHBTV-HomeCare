package Project.HouseService.Repository;

import Project.HouseService.Entity.ChatConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatConversationRepository extends JpaRepository<ChatConversation, Long> {
    Optional<ChatConversation> findTopByUserIdOrderByIdDesc(Long userId);
}
