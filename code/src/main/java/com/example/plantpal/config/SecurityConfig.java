package com.example.plantpal.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final RoleBasedLoginSuccessHandler loginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // หน้าที่เข้าได้โดยไม่ต้อง login
                .requestMatchers("/", "/login", "/register", "/error",
                                 "/css/**", "/js/**", "/img/**").permitAll()
                // เฉพาะ ADMIN ถ้าไม่ใช่จะได้ 403 -> error.html
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(form -> form
                .loginPage("/login")              // ใช้หน้า auth/login.html ของเรา
                .successHandler(loginSuccessHandler)
                .failureHandler((request, response, ex) -> response.sendRedirect(
                        request.getContextPath()
                        + (ex instanceof DisabledException ? "/login?disabled" : "/login?error")))
                .permitAll())
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll());
        return http.build();
    }
}
