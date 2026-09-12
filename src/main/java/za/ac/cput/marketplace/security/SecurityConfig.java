package za.ac.cput.marketplace.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Open to everyone, no token needed
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/upload/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/*.jpg", "/*.png", "/*.jpeg").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/listings").permitAll()

                        // Everything else requires a valid token (checked further in each controller)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));
        return http.build();
    }
}