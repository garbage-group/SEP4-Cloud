package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.CreateUserDto;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.JwtService;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.repository.IUserRepository;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import static org.mockito.Mockito.when;
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

    @Test
    public void create_UserWhoIsGarbageCollector_returnsSavedUser() {
        //Arrange
        CreateUserDto createUserDto = new CreateUserDto(
                "tester_name",
                "Tester Testington",
                "testword",
                "Garbage Collector",
                "testion"
        );
        User user = new User(
                "testerion",
                "Tester Testington",
                "testword",
                "Garbage Collector",
                "testion"
        );
        List<User> users = new ArrayList<>();
        users.add(user);

        //Mock
        when(userRepository.findAll()).thenReturn(users);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");

        //Act
        User result = userService.create(createUserDto);

        //Assert
        assertEquals(result, user);
    }

    @Test
    public void create_UserWhoIsNotGarbageCollector_throwsException() {
        //Arrange
        CreateUserDto createUserDto = new CreateUserDto(
                "tester_name",
                "Tester Testington",
                "testword",
                "Harry Tester",
                "testion"
        );
        User user = new User(
                "admin",
                "Tester Testington",
                "testword",
                "Harry Tester",
                "testion"
        );
        List<User> users = new ArrayList<>();
        users.add(user);

        //Mock
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(userRepository.findAll()).thenReturn(users);

        // Act and assert
        assertThrows(IllegalArgumentException.class, () -> userService.create(createUserDto));
    }

    @Test
    public void create_UserWithExistingUsername_throwsDuplicateException() {
        //Arrange
        CreateUserDto createUserDto = new CreateUserDto(
                "admin",
                "Tester Testington",
                "testword",
                "Harry Tester",
                "testion"
        );
        User user = new User(
                "admin",
                "Tester Testington",
                "testword",
                "Harry Tester",
                "testion"
        );
        List<User> users = new ArrayList<>();
        users.add(user);

        //Mock
        when(userRepository.findAll()).thenReturn(users);

        // Act and assert
        assertThrows(DuplicateKeyException.class, () -> userService.create(createUserDto));
    }

    @Test
    public void testFetchAllUsers_NoUsersFound() {
        // Given
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // When & Then (Exception handling)
        assertThrows(RuntimeException.class, () -> userService.fetchAllUsers());
        // Add assertions or further handling for exception scenarios if needed
    }
}
