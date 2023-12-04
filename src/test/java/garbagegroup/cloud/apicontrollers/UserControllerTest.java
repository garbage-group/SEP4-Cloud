package garbagegroup.cloud.apicontrollers;

import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.controller.UserController;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationResponse authenticationResponse; // Mock AuthenticationResponse as needed

    @InjectMocks
    private UserController userController;

    @InjectMocks
    private DTOConverter dtoConverter;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void authenticate_ReturnsOkResponse() {
        // Given
        UserDto userDto = new UserDto(); // Create or mock a UserDto object as needed

        // Mock behavior
        when(userService.authenticate(userDto)).thenReturn(authenticationResponse);

        // When
        ResponseEntity<AuthenticationResponse> responseEntity = userController.authenticate(userDto);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(authenticationResponse, responseEntity.getBody());
        verify(userService, times(1)).authenticate(userDto);
    }

    @Test
    public void fetchUserByUsername_ReturnsUserDto() {
        // Given
        String username = "existinguser";
        User user = new User("existinguser", "password", "Full Name", "ROLE_USER", "Region");

        // Mock behavior
        when(userService.fetchUserByUsername(username)).thenReturn(user);

        // When
        ResponseEntity<UserDto> responseEntity = userController.fetchUserByUsername(username);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(user.getUsername(), responseEntity.getBody().getUsername());
        // Add assertions for other fields as needed
        verify(userService, times(1)).fetchUserByUsername(username);
    }

    @Test
    public void authenticate_ThrowsException_ReturnsNotFound() {
        // Given
        UserDto userDto = new UserDto(); // Create or mock a UserDto object as needed

        // Mock behavior to throw an exception
        when(userService.authenticate(userDto)).thenThrow(new RuntimeException("Authentication failed"));

        // When
        ResponseEntity<AuthenticationResponse> responseEntity = userController.authenticate(userDto);

        // Then
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
    }

    @Test
    public void fetchUserByUsername_ThrowsException_ReturnsBadRequest() {
        // Given
        String username = "nonexistentuser";

        // Mock behavior to throw an exception
        when(userService.fetchUserByUsername(anyString())).thenThrow(new RuntimeException("User not found"));

        // When
        ResponseEntity<UserDto> responseEntity = userController.fetchUserByUsername(username);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }

    @Test
    public void fetchAllUsers_ThrowsException_ReturnsBadRequest() {
        // Mock behavior to throw an exception
        when(userService.fetchAllUsers()).thenThrow(new RuntimeException("Error retrieving users"));

        // When
        ResponseEntity<List<UserDto>> responseEntity = userController.fetchAllUsers();

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
    }
}
