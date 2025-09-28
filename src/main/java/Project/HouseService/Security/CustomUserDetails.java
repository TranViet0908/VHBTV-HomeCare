// src/main/java/Project/HouseService/Security/CustomUserDetails.java
package Project.HouseService.Security;

import Project.HouseService.Entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {
    private final User u;

    public CustomUserDetails(User u) { this.u = u; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Vendor có thêm quyền customer để mua/đặt dịch vụ của vendor khác
        if (u.getRole() == User.Role.ROLE_VENDOR) {
            return List.of(
                    new SimpleGrantedAuthority("ROLE_VENDOR"),
                    new SimpleGrantedAuthority("ROLE_CUSTOMER")
            );
        }
        return List.of(new SimpleGrantedAuthority(u.getRole().name()));
    }

    @Override public String getPassword() { return u.getPassword(); }
    @Override public String getUsername() { return u.getUsername(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return Boolean.TRUE.equals(u.getActive()); }

    public Long getId() { return u.getId(); }
    public User getUser() { return u; }
}
