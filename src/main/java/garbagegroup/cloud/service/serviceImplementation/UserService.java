package garbagegroup.cloud.service.serviceImplementation;


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

import java.util.NoSuchElementException;


@Service
public class UserService implements IUserService {

    private final JwtService jwtService;
    private IUserRepository IUserRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;



    @Autowired
    public UserService(JwtService jwtService, IUserRepository IUserRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.IUserRepository = IUserRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setUsername(user.getUsername());
        userDto.setPassword(user.getPassword());
        userDto.setRole(user.getRole());
        userDto.setFullname(user.getFullname());

        return userDto;
    }

    @Override
    public User fetchUserByUsername(String username) {
        User user = IUserRepository.findByUsername(username);
        if (user != null) {
            System.out.println("from service"+user.getUsername());
            return user;
        } else {
            throw new RuntimeException("User with username " + username + " not found");
        }
    }

    @Override
    public void deleteByUsername(String username) {
        if (IUserRepository.existsById(username)) {
            if (IUserRepository.findByUsername(username).getRole().equalsIgnoreCase("municipality worker"))
                throw new IllegalArgumentException("You may only delete garbage collectors! No other roles.");
            IUserRepository.deleteById(username);
        } else throw new NoSuchElementException("User with username '" + username + "' not found");
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
