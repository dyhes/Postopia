package com.heslin.postopia.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager, PostopiaAuthenticationFilter postopiaAuthenticationFilter) throws Exception {
        http
        .cors(Customizer.withDefaults())
        .csrf(c -> c.disable())
        .authorizeHttpRequests(
            authorize -> authorize
            .requestMatchers( "/auth/**", "/open/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilter(postopiaAuthenticationFilter)
        .sessionManagement(sessionManagementCustomizer -> sessionManagementCustomizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        ;
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
}
