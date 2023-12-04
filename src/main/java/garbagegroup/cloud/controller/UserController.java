package garbagegroup.cloud.controller;

import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;

import garbagegroup.cloud.model.User;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import garbagegroup.cloud.service.serviceInterface.IUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController

@CrossOrigin
public class UserController {

    private final IUserService userService;
    private final UserService service;
    private DTOConverter dtoConverter;

    private Logger logger = LoggerFactory.getLogger(UserController.class);


    @Autowired
    public UserController(IUserService userService, UserService service) {
        this.userService = userService;
        this.service = service;
        this.dtoConverter = new DTOConverter();
    }


    @GetMapping("/users/{username}")
    @PreAuthorize("hasRole('garbage collector')")
    public ResponseEntity<UserDto> fetchUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.fetchUserByUsername(username);
            UserDto userDto = dtoConverter.convertToUserDto(user); // Call the convertToUserDto method from UserService
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
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

    @GetMapping("/users")
    public ResponseEntity<?> fetchAllUsers() {
        try {
            return new ResponseEntity<>(userService.fetchAllUsers(), HttpStatus.OK);
        } catch (RuntimeException e) {
            logger.error("Error retrieving all users", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }


}
