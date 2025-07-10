package com.tsc.config;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.tsc.service.CustomUserDetailsService;

import jakarta.annotation.PostConstruct;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private Environment environment;

    @PostConstruct
    public void logActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            activeProfiles = environment.getDefaultProfiles();
        }
        logger.info("Security Config - Active profiles: {}", Arrays.toString(activeProfiles));
        boolean isLocalProfile = Arrays.asList(activeProfiles).contains("local");
        logger.info("H2 Console will be enabled: {}", isLocalProfile);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring Security Filter Chain");

        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            activeProfiles = environment.getDefaultProfiles();
        }
        boolean isLocalProfile = Arrays.asList(activeProfiles).contains("local");

        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/login", "/perform_login", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .requestMatchers("/api/reset").hasRole("ADMIN")
                        .requestMatchers(isLocalProfile ? "/h2-console/**" : "/no-match").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/perform_login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll())
                .exceptionHandling((exceptions) -> exceptions
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.error("Access denied for request: {}", request.getRequestURI());
                            response.sendRedirect("/login?error=true");
                        }));

        if (isLocalProfile) {
            http.csrf(csrf -> csrf
                            .ignoringRequestMatchers("/h2-console/**"))
                    .headers(headers -> headers
                            .frameOptions(frameOptions -> frameOptions.sameOrigin()));
            logger.info("CSRF and frame options configured for H2 console (local profile)");
        } else {
            logger.info("CSRF enabled for all endpoints in prod profile");
        }

        logger.info("Security configuration completed for profiles: {}", Arrays.toString(activeProfiles));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            activeProfiles = environment.getDefaultProfiles();
        }
        boolean isLocalProfile = Arrays.asList(activeProfiles).contains("local");

        if (isLocalProfile) {
            logger.info("Bypassing Spring Security for /h2-console/** in local profile");
            return (web) -> web.ignoring().requestMatchers("/h2-console/**");
        }
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/webjars/**");
    }
}