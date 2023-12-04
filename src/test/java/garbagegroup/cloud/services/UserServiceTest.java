package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.JwtService;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.repository.IUserRepository;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertToUserDto() {
        // Given
        User user = new User();
        user.setUsername("testUser");
        user.setPassword("password");
        user.setRole("role");
        user.setFullname("fullname");

        // When
        UserDto userDto = userService.convertToUserDto(user);

        // Then
        assertEquals("testUser", userDto.getUsername());
        assertEquals("password", userDto.getPassword());
        assertEquals("role", userDto.getRole());
        assertEquals("fullname", userDto.getFullname());
    }

    @Test
    void testFetchUserByUsername_UserExists() {
        // Given
        User user = new User();
        user.setUsername("testUser");
        when(userRepository.findByUsername("testUser")).thenReturn(user);

        // When
        User fetchedUser = userService.fetchUserByUsername("testUser");

        // Then
        assertEquals("testUser", fetchedUser.getUsername());
    }

    @Test
    void testFetchUserByUsername_UserDoesNotExist() {
        // Given
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(null);

        // When & Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.fetchUserByUsername("nonExistentUser")
        );

        assertEquals("User with username nonExistentUser not found", exception.getMessage());
    }

    @Test
    void testDeleteByUsername_UserExistsAndIsNotMunicipalityWorker() {
        // Given
        User user = new User();
        user.setUsername("username");
        user.setRole("garbage collector");

        when(userRepository.existsById("username")).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(user);

        // When
        userService.deleteByUsername("username");

        // Then
        verify(userRepository, times(1)).deleteById("username");
    }

    @Test
    void testDeleteByUsername_UserDoesNotExist() {
        // Given
        when(userRepository.existsById("nonExistentUser")).thenReturn(false);

        // When & Then
        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> userService.deleteByUsername("nonExistentUser")
        );

        assertEquals("User with username 'nonExistentUser' not found", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testDeleteByUsername_UserIsMunicipalityWorker() {
        // Given
        User user = new User();
        user.setUsername("username");
        user.setRole("municipality worker");

        when(userRepository.existsById("username")).thenReturn(true);
        when(userRepository.findByUsername("username")).thenReturn(user);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.deleteByUsername("username")
        );

        assertEquals("You may only delete garbage collectors! No other roles.", exception.getMessage());
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void testAuthenticate_ValidUserCredentials_ReturnsAuthenticationResponse() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("validUser");
        userDto.setPassword("validPassword");

        User user = new User();
        user.setUsername("validUser");
        when(userRepository.findByUsername("validUser")).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("generatedToken");

        // When
        var authResponse = userService.authenticate(userDto);

        // Then
        assertNotNull(authResponse);
        assertEquals("generatedToken", authResponse.getToken());
    }

    @Test
    void testAuthenticate_InvalidUserCredentials_ThrowsException() {
        // Given
        UserDto userDto = new UserDto();
        userDto.setUsername("invalidUser");
        userDto.setPassword("invalidPassword");

        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Authentication failed"));
        when(userRepository.findByUsername("invalidUser")).thenReturn(null);

        // When & Then
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.authenticate(userDto)
        );

        assertEquals("Authentication failed", exception.getMessage());
    }
}

