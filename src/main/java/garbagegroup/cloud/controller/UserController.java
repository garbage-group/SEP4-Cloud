package garbagegroup.cloud.controller;

import garbagegroup.cloud.DTOs.CreateUserDto;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;

import garbagegroup.cloud.model.User;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import garbagegroup.cloud.service.serviceInterface.IUserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@CrossOrigin
public class UserController {
    private final IUserService userService;
    private final UserService service;
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(IUserService userService, UserService service) {
        this.userService = userService;
        this.service = service;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> fetchUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.fetchUserByUsername(username);
            UserDto userDto = service.convertToUserDto(user); // Call the convertToUserDto method from UserService
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody UserDto request) {
        try {
            return ResponseEntity.ok(service.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserDto createUserDto) {
        try {
            User user = userService.create(createUserDto);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (DuplicateKeyException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IllegalArgumentException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
