package garbagegroup.cloud.service.serviceImplementation;


import garbagegroup.cloud.dto.UserDto;
import garbagegroup.cloud.jwt.JwtService;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.repository.UserRepository;
import garbagegroup.cloud.service.serviceInterface.IUserService;
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
    private UserRepository userRepository;
    private final List<UserDto> store;


    @Autowired
    public UserService(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        // Initialize store here
        this.store = new ArrayList<>();
        this.store.add(new UserDto("municipalityWorker", "municipalityWorker", "municipalityWorker", "municipalityWorker"));
        // Add more users to the store if needed
    }

    @Override
    public User fetchUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            throw new RuntimeException("User with username " + username + " not found");
        }
    }

    public AuthenticationResponse authenticate(UserDto request) {

        System.out.println(request.getUsername());

        UserDto userFromDb = store.stream().
                filter(user -> user.getUsername()
                        .equals(request.getUsername())
                ).findFirst().orElseThrow(() -> new RuntimeException("User not found"));

        User user = new User();
        user.setUsername(userFromDb.getUsername());
        System.out.println(userFromDb.getUsername());
        user.setPassword(userFromDb.getPassword());
        user.setFullname(userFromDb.getFullname());
        user.setRole(userFromDb.getRole());
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

    }

}
