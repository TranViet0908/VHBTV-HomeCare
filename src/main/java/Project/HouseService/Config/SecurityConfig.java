// src/main/java/Project/HouseService/Config/SecurityConfig.java  (sau khi sửa)
package Project.HouseService.Config;

import Project.HouseService.Security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import java.util.List;

@Configuration
public class SecurityConfig {

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
        repo.setParameterName("_csrf");
        var handler = new CsrfTokenRequestAttributeHandler();
        handler.setCsrfRequestAttributeName("_csrf");
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
    public SecurityFilterChain appChain(HttpSecurity http,
                                        DaoAuthenticationProvider appAuthProvider) throws Exception {
        http.authenticationProvider(appAuthProvider);
        applyCsrf(http);

        // Bỏ CSRF cho IPN VNPAY bằng RegexRequestMatcher (không dùng AntPath/Mvc*)
        RequestMatcher vnpIpnPost = new RegexRequestMatcher("^/customer/payment/vnpay/ipn$", "POST");
        RequestMatcher vnpIpnGet  = new RegexRequestMatcher("^/customer/payment/vnpay/ipn$", "GET");
        http.csrf(csrf -> csrf.ignoringRequestMatchers(vnpIpnPost, vnpIpnGet));

        // PUBLIC cho đúng 2 route chi tiết vendor và API tóm tắt
        List<RequestMatcher> publicVendor = List.of(
                new RegexRequestMatcher("^/vendor/id/\\d+$", "GET"),
                new RegexRequestMatcher("^/vendor/(?!dashboard$|profile$|orders$|services$|coupons$|reviews$|media$|settings$)[^/]+$", "GET"),
                new RegexRequestMatcher("^/api/vendors/.*$", "GET")
        );

        http.authorizeHttpRequests(reg -> reg
                .requestMatchers("/", "/login", "/register",
                        "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico",
                        "/customer/chat", "/customer/chat/**",
                        "/customer/chatbot", "/customer/chatbot/**",
                        "/api/chat/**"
                ).permitAll()

                // Callback/return thanh toán VNPAY phải PUBLIC
                .requestMatchers(HttpMethod.GET,
                        "/customer/payment/vnpay/return",
                        "/customer/payment/vnpay-return"
                ).permitAll()
                // IPN VNPAY PUBLIC
                .requestMatchers(HttpMethod.GET,  "/customer/payment/vnpay/ipn").permitAll()
                .requestMatchers(HttpMethod.POST, "/customer/payment/vnpay/ipn").permitAll()

                // KHÓA console vendor trước
                .requestMatchers(
                        "/vendor/dashboard", "/vendor/dashboard/**",
                        "/vendor/profile",   "/vendor/profile/**",
                        "/vendor/orders",    "/vendor/orders/**",
                        "/vendor/services",  "/vendor/services/**",
                        "/vendor/coupons",   "/vendor/coupons/**",
                        "/vendor/reviews",   "/vendor/reviews/**",
                        "/vendor/media",     "/vendor/media/**",
                        "/vendor/settings",  "/vendor/settings/**"
                ).hasAnyRole("VENDOR","ADMIN")

                // MỞ PUBLIC cho detail + root + API tóm tắt
                .requestMatchers(HttpMethod.GET, "/vendor", "/vendor/").permitAll()
                .requestMatchers(HttpMethod.GET, "/vendor/*", "/vendor/id/*").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/vendors/**").permitAll()

                // Phần còn lại của /vendor/**
                .requestMatchers("/vendor/**").hasAnyRole("VENDOR","ADMIN")

                // Khu vực customer
                .requestMatchers("/customer/**").hasAnyRole("CUSTOMER","VENDOR","ADMIN")

                // Còn lại
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
