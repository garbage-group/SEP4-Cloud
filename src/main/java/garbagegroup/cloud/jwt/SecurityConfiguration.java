package garbagegroup.cloud.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Configuration class responsible for setting up security configurations and defining security filter chains.
 */

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration{
    private final JwtAuthenticationFilter jwtAuthFilter;

    /**
     * Configures the security filter chain for various HTTP endpoints and methods.
     *
     * @param http The HttpSecurity object to configure security settings.
     * @return The configured SecurityFilterChain for handling security within the application.
     * @throws Exception If an exception occurs during security configuration setup.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/users/authenticate").permitAll()
                        .requestMatchers(HttpMethod.POST,"/users").hasAuthority("municipality worker")
                        .requestMatchers(HttpMethod.DELETE,"/users").hasAuthority("municipality worker")
                        .requestMatchers(HttpMethod.PATCH,"/users/{username}").hasAuthority("municipality worker")
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
