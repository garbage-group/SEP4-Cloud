package garbagegroup.cloud.controller;

import garbagegroup.cloud.dto.UserDto;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;

import garbagegroup.cloud.model.User;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import garbagegroup.cloud.service.serviceInterface.IUserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController

@CrossOrigin
public class UserController {

    private final IUserService userService;
    private final UserService service;


    @Autowired
    public UserController(IUserService userService, UserService service) {
        this.userService = userService;
        this.service = service;
    }


    @GetMapping("/{username}")
    public ResponseEntity<UserDto> fetchUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.fetchUserByUsername(username);
            UserDto userDto = convertToUserDto(user);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    // Helper method to convert User entity to UserDto
    private UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setRole(user.getRole());
        userDto.setFullname(user.getFullname());
        return userDto;
    }

    @PostMapping("/users/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody UserDto request) {
        try {
            return ResponseEntity.ok(service.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }


}
