package garbagegroup.cloud.apicontrollers;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationResponse authenticationResponse; // Mock AuthenticationResponse as needed

    @InjectMocks
    private UserController userController;

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
    public void fetchUserByUsername_ReturnsUserDto() {
        // Given
        String username = "testuser";
        User user = new User(); // Mock User object
        UserDto userDto = new UserDto(); // Create a UserDto object or mock as needed

        // Mock behavior
        when(userService.fetchUserByUsername(username)).thenReturn(user);
        // Assume a service method convertToUserDto exists, or mock it accordingly
        when(userService.convertToUserDto(user)).thenReturn(userDto);

        // When
        ResponseEntity<UserDto> responseEntity = userController.fetchUserByUsername(username);

        // Then
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDto, responseEntity.getBody());
        verify(userService, times(1)).fetchUserByUsername(username);
        verify(userService, times(1)).convertToUserDto(user);
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
}
