// src/main/java/Project/HouseService/Repository/NotificationRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
