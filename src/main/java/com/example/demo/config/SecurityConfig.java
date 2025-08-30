package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                // CSRF 보호 활성화 (특정 경로만 예외)
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/user/login", // CSRF 보호 무시 경로 추가
                                "/user/signup",
                                "/memorial/comment/**", // 댓글 등록
                                "/memorial/write", // 추모글 작성
                                "/api/ai/ask",
                                "/requests/apply",
                                "/deceased_create",
                                "/requests_dashboard"
                        )
                )
                .formLogin(f -> f.disable())

                .httpBasic(b -> b.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> res.sendRedirect("/user/login"))
                        .accessDeniedHandler((req, res, e) -> res.sendRedirect("/user/login"))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/main",
                                "/error",
                                "/favicon.ico",
                                "/*.html","/*.css","/*.js","/*.ico",
                                "/*.png","/*.jpg","/*.jpeg","/*.gif",
                                "/*.svg","/*.webp","/*.woff","/*.woff2","/*.ttf",
                                "/static/**","/css/**","/js/**","/images/**",
                                "/webjars/**",
                                "/uploads/**",
                                "/files/**",
                                "/about",
                                "/process",
                                "/user/login"
                        ).permitAll()
                        .requestMatchers(
                                "/user/login","/user/signup","/user/me",
                                "/api/users/login","/api/users/signup","/api/user/login","/api/users/me",
                                "/api/ping"
                        ).permitAll()
                        .requestMatchers(
                                "/memorial",
                                "/memorial/",
                                "/memorial/detail/**",
                                "/memorial/password_check/**",
                                "/memorial/comment/**",
                                "/user/auth/status",
                                "/api/ai/ask"
                        ).permitAll()
                        .requestMatchers(
                                "/memorial/memorial_write",
                                "/memorial/write"
                        ).authenticated()

                        .requestMatchers("/requests/apply").authenticated()
                        .requestMatchers("/requests", "/dashboard").authenticated()
                        .requestMatchers("/deceased/**").authenticated()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());
        return http.build();
    }
}