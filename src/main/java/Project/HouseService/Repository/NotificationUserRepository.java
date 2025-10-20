// src/main/java/Project/HouseService/Repository/NotificationUserRepository.java
package Project.HouseService.Repository;

import Project.HouseService.Entity.NotificationUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {

    @Query("""
           select nu from NotificationUser nu
             join fetch nu.notification n
            where nu.user.id = :userId and nu.isDeleted = false
            order by n.createdAt desc, nu.id desc
           """)
    List<NotificationUser> findPageByUser(@Param("userId") Long userId, Pageable pageable);

    long countByUser_IdAndIsDeletedFalseAndIsReadFalse(Long userId);

    Optional<NotificationUser> findByIdAndUser_Id(Long id, Long userId);

    @Modifying
    @Query("update NotificationUser nu set nu.isRead = true, nu.readAt = CURRENT_TIMESTAMP " +
            "where nu.user.id = :userId and nu.isDeleted = false and nu.isRead = false")
    int markAllRead(@Param("userId") Long userId);
}
