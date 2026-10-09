package com.example.plantpal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final RoleBasedLoginSuccessHandler loginSuccessHandler;
    private final ApiSecurityErrorHandler apiSecurityErrorHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        RequestMatcher api = PathPatternRequestMatcher.withDefaults().matcher("/api/**");
        http
            .authorizeHttpRequests(auth -> auth
                // หน้าที่เข้าได้โดยไม่ต้อง login
                .requestMatchers("/", "/login", "/register", "/error",
                                 "/css/**", "/js/**", "/img/**").permitAll()
                
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // อันนี้มันเฉพาะ ADMIN ถ้าไม่ใช่จะได้ 403 นะ
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")              
                .successHandler(loginSuccessHandler)
                .failureHandler((request, response, ex) -> response.sendRedirect(
                        request.getContextPath()
                        + (ex instanceof DisabledException ? "/login?disabled" : "/login?error")))
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll())
        
            .csrf(csrf -> csrf.ignoringRequestMatchers(api))
            .exceptionHandling(ex -> ex
                .defaultAuthenticationEntryPointFor(apiSecurityErrorHandler, api)
                .defaultAccessDeniedHandlerFor(apiSecurityErrorHandler, api));
        return http.build();
    }
}
