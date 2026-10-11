package com.anurag.lms.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.anurag.lms.filter.JwtAuthenticationFilter;
import com.anurag.lms.repository.MemberRepository;
import com.anurag.lms.service.JwtService;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Which origins are allowed to call the API - the Vite dev server locally,
    // the deployed frontend's URL in production. Comma-separated, see application.properties.
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") String allowedOrigins) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, JwtService jwtService, MemberRepository memberRepository) throws Exception {

        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtService, memberRepository);

        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/members/register", "/members/login").permitAll()

                        // members: staff can list, admin can change roles / delete
                        .requestMatchers(HttpMethod.GET, "/members").hasAnyRole("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/members/*/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/members/*").hasRole("ADMIN")

                        // books & categories: everyone signed in can read, staff can change
                        .requestMatchers(HttpMethod.GET, "/books/**", "/categories/**").authenticated()
                        .requestMatchers("/books/**", "/categories/**").hasAnyRole("ADMIN", "LIBRARIAN")

                        // borrowing: a member sees their own loans, staff run the desk
                        .requestMatchers("/borrow/my").authenticated()
                        .requestMatchers("/borrow/**").hasAnyRole("ADMIN", "LIBRARIAN")

                        .anyRequest().authenticated())
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
