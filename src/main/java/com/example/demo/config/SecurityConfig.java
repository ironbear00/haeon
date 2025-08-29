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

    /** CORS 설정 */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // 로컬 프런트 허용
        config.setAllowedOriginPatterns(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
        // 메서드 허용
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        // 헤더 허용 (쿠키/JSON 요청 시 필요)
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept"));
        // 응답 헤더 노출(필요 시)
        config.setExposedHeaders(List.of("Location"));
        // 쿠키 허용
        config.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /** 보안 필터 체인 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS 프리플라이트 전부 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ 공개 엔드포인트
                        .requestMatchers(
                                "/", "/main",
                                "/error",
                                "/user/login", "/user/signup",
                                "/api/users/login", "/api/users/signup", "/api/user/login", "/api/ping",
                                "/memorial", "/memorial/**",
                                "/mypage/info", "/mypage/**",
                                "/user/me", "/api/users/me",
                                "/user/auth/status"
                                // ✅ AI API 공개
                        ).permitAll()

                        // ✅ 정적 리소스
                        .requestMatchers(
                                "/*.html", "/*.css", "/*.js", "/*.ico",
                                "/*.png", "/*.jpg", "/*.jpeg", "/*.gif",
                                "/*.svg", "/*.webp", "/*.woff", "/*.woff2", "/*.ttf",
                                "/static/**", "/css/**", "/js/**",
                                "/images/**", "/webjars/**"
                        ).permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/user/logout")
                        .logoutSuccessUrl("/user/login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}