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
    public ResponseEntity<User> fetchUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.fetchUserByUsername(username);
            System.out.println(user.getUsername());
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/users/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody UserDto request) {
        try {
            System.out.println(request.getUsername());
            return ResponseEntity.ok(service.authenticate(request));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }



}
