package garbagegroup.cloud.controller;

import garbagegroup.cloud.DTOs.UpdateBinDto;
import garbagegroup.cloud.DTOs.UpdateUserDto;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;

import garbagegroup.cloud.model.User;
import garbagegroup.cloud.service.serviceImplementation.UserService;
import garbagegroup.cloud.service.serviceInterface.IUserService;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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


    @GetMapping("/users/{username}")
    public ResponseEntity<UserDto> fetchUserByUsername(@PathVariable("username") String username) {
        try {
            User user = userService.fetchUserByUsername(username);
            UserDto userDto = service.convertToUserDto(user); // Call the convertToUserDto method from UserService
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

        @PreAuthorize("hasRole('municipality worker')")
        @PatchMapping("/users/{username}")
        public ResponseEntity<UpdateUserDto> updateUser(@PathVariable("username") String username, @RequestBody UpdateUserDto userDto) {
            try {
                User user = userService.fetchUserByUsername(username);
                user.setFullname(userDto.getFullname());
                user.setPassword(userDto.getPassword());
                user.setRegion(userDto.getRegion());
                userService.updateUser(userDto);
                return new ResponseEntity<>(userDto, HttpStatus.OK);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

        }

//    @PatchMapping("/users/{username}")
//    @PreAuthorize("hasRole(userService.fetchUserByUsername(userDto.username).equals('municipality worker'))")
//    public ResponseEntity<UpdateUserDto> updateUser(
//            @PathVariable("username") String username,@RequestBody UpdateUserDto userDto,
//            Authentication authentication) {
//        try {
//                if (authentication.getAuthorities().stream()
//                        .anyMatch(grantedAuthority ->
//                                grantedAuthority.getAuthority().equalsIgnoreCase("municipality worker"))) {
//                    // If the user doesn't have the required role, deny access
//                    User user = userService.fetchUserByUsername(username);
//                    user.setFullname(userDto.getFullname());
//                    user.setPassword(userDto.getPassword());
//                    user.setRegion(userDto.getRegion());
//                    userService.updateUser(user);
//                    return new ResponseEntity<>(userDto, HttpStatus.OK);
//                } else {
//                    return new ResponseEntity<>(HttpStatus.FORBIDDEN);
//                }
//        } catch (Exception e) {
//            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//        }
//    }
}
