package com.eventbooking.security;

import com.vaadin.flow.spring.security.VaadinWebSecurity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends VaadinWebSecurity {

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
                // Allow H2 console
                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers("/h2-console/**").permitAll());

                http.headers(headers -> headers
                                .frameOptions(frame -> frame.sameOrigin()));

                http.csrf(csrf -> csrf
                                .ignoringRequestMatchers("/h2-console/**"));

                // Call super to apply Vaadin's default security configuration
                super.configure(http);

                // Set login view
                setLoginView(http, "/login");
        }
}
