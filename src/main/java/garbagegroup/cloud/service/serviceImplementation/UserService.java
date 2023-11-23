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


@Service
public class UserService implements IUserService {

    private final JwtService jwtService;
    private UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;



    @Autowired
    public UserService(JwtService jwtService, UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;

//        User user = new User("admin", "admin", "admin", "admin");
//        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
//        userRepository.save(user);
    }

    @Override
    public User fetchUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            System.out.println("from service"+user.getUsername());
            return user;
        } else {
            throw new RuntimeException("User with username " + username + " not found");
        }
    }

    public AuthenticationResponse authenticate(UserDto request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByUsername(request.getUsername());

        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


}
