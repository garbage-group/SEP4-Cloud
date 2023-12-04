package garbagegroup.cloud.services;

import garbagegroup.cloud.DTOs.CreateUserDto;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
public class UserServiceTest {
    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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
