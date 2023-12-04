package garbagegroup.cloud.service.serviceImplementation;


import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.JwtService;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.repository.IUserRepository;
import garbagegroup.cloud.service.serviceInterface.IUserService;
import jakarta.validation.constraints.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserService implements IUserService {

    private final JwtService jwtService;
    private IUserRepository IUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private DTOConverter dtoConverter;



    @Autowired
    public UserService(JwtService jwtService, IUserRepository IUserRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.IUserRepository = IUserRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.dtoConverter = new DTOConverter();

//        User user = new User("admin", "password", "admin", "municipality worker", "horsens");
//        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
//        IUserRepository.save(user);
    }


    @Override
    public User fetchUserByUsername(String username) {
        User user = IUserRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            throw new RuntimeException("User with username " + username + " not found");
        }
    }

    @Override
    public List<UserDto> fetchAllUsers() {
        List<User> users = IUserRepository.findAll();
        List<UserDto> userDtos= new ArrayList<>();
        if (users.isEmpty()) {
            throw new RuntimeException("No users found");
        }
        for (User user : users) {
            userDtos.add(dtoConverter.convertToUserDto(user));
        }
        return userDtos;
    }

    public AuthenticationResponse authenticate(UserDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = IUserRepository.findByUsername(request.getUsername());

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


}
