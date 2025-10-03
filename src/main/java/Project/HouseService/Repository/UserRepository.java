package Project.HouseService.Repository;

import Project.HouseService.Entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(User.Role role);

    // mới: đếm theo role để chặn đăng ký admin lần 2
    long countByRole(User.Role role);

    @Query("""
           select u from User u
           where (:kw is null or :kw = '' 
                  or lower(u.username) like lower(concat('%', :kw, '%'))
                  or lower(u.email) like lower(concat('%', :kw, '%'))
                  or lower(cast(u.role as string)) like lower(concat('%', :kw, '%')))
           order by u.id asc
           """)
    Page<User> search(String kw, Pageable pageable);
    java.util.List<Project.HouseService.Entity.User> findTop200ByRoleOrderByIdAsc(Project.HouseService.Entity.User.Role role);
    java.util.List<Project.HouseService.Entity.User> findByRoleOrderByIdAsc(Project.HouseService.Entity.User.Role role);
    Optional<User> findByUsernameAndRole(String username, User.Role role);

    @Query("""
      select u from User u
      where (:q is null or :q = '' 
         or lower(u.username) like lower(concat('%', :q, '%'))
         or lower(u.email)    like lower(concat('%', :q, '%'))
         or u.phone           like concat('%', :q, '%'))
      order by u.id desc
    """)
    Page<User> searchCustomers(@Param("q") String q, Pageable pageable);

}
