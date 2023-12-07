package garbagegroup.cloud.jwt;

import garbagegroup.cloud.repository.IUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuration class responsible for defining various beans related to authentication and security.
 * It configures authentication providers, user details service, password encoder, and authentication manager.
 */
@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {
    @Autowired
    private IUserRepository IUserRepository;

    /**
     * Defines a bean for the user details service that retrieves user information from the repository.
     *
     * @return An instance of UserDetailsService that fetches user details from IUserRepository.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> IUserRepository.findByUsername(username);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines a bean for the authentication manager using the provided AuthenticationConfiguration.
     *
     * @param config AuthenticationConfiguration instance to obtain the authentication manager.
     * @return An AuthenticationManager instance used for authentication operations.
     * @throws Exception If an exception occurs while retrieving the authentication manager from the configuration.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
