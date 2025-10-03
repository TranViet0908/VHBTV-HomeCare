// src/main/java/Project/HouseService/Config/SecurityConfig.java
package Project.HouseService.Config;

import Project.HouseService.Security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

@Configuration
public class SecurityConfig {

    // BẮT BUỘC: dùng resolver chuẩn (tránh lỗi multipart + CSRF)
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public DaoAuthenticationProvider adminAuthProvider(CustomUserDetailsService uds, PasswordEncoder encoder) {
        var p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(encoder);
        return p;
    }

    @Bean
    public DaoAuthenticationProvider appAuthProvider(PasswordEncoder encoder, Project.HouseService.Repository.UserRepository users) {
        var p = new DaoAuthenticationProvider();
        p.setPasswordEncoder(encoder);
        p.setUserDetailsService(raw -> {
            String key = raw == null ? "" : raw.trim();
            var u = users.findByUsername(key).orElseGet(() -> key.contains("@") ? users.findByEmail(key).orElse(null) : null);
            if (u == null || u.getRole() == Project.HouseService.Entity.User.Role.ROLE_ADMIN)
                throw new org.springframework.security.core.userdetails.UsernameNotFoundException("User not found");
            return new Project.HouseService.Security.CustomUserDetails(u);
        });
        return p;
    }

    private void applyCsrf(HttpSecurity http) throws Exception {
        var repo = new HttpSessionCsrfTokenRepository();
        repo.setParameterName("_csrf");          // tên field mà form gửi
        var handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf"); // để Thymeleaf đọc ${_csrf.token}
        http.csrf(csrf -> csrf
                .csrfTokenRepository(repo)
                .csrfTokenRequestHandler(handler)
        );
    }

    @Bean
    @Order(1)
    public SecurityFilterChain adminChain(HttpSecurity http, DaoAuthenticationProvider adminAuthProvider) throws Exception {
        http.securityMatcher("/admin/**");
        http.authenticationProvider(adminAuthProvider);
        applyCsrf(http);

        http.authorizeHttpRequests(reg -> reg
                .requestMatchers("/admin/login",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "Share/**"
                ).permitAll()
                .anyRequest().hasRole("ADMIN")
        );

        http.formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin/dashboard", true)
                .failureUrl("/admin/login?error")
                .permitAll()
        );

        http.logout(l -> l
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login?logout")
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.httpBasic(b -> b.disable());
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http, DaoAuthenticationProvider appAuthProvider) throws Exception {
        http.authenticationProvider(appAuthProvider);
        applyCsrf(http);

        http.authorizeHttpRequests(reg -> reg
                // mở cả GET trang chat và POST gửi tin
                .requestMatchers(
                        "/customer/chat", "/customer/chat/**",
                        "/customer/chatbot", "/customer/chatbot/**",
                        "/api/chat/**"
                ).permitAll()

                .requestMatchers("/", "/login", "/register",
                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico").permitAll()
                .requestMatchers("/vendor/**").hasAnyRole("VENDOR","ADMIN")
                .requestMatchers("/customer/**").hasAnyRole("CUSTOMER","VENDOR","ADMIN")
                .anyRequest().permitAll()
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/", true)
                .failureUrl("/login?error")
                .permitAll()
        );

        http.logout(l -> l
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .deleteCookies("JSESSIONID")
                .permitAll()
        );

        http.httpBasic(b -> b.disable());
        return http.build();
    }
}
