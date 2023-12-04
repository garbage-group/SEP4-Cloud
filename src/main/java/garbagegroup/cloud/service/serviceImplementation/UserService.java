package garbagegroup.cloud.service.serviceImplementation;


import garbagegroup.cloud.DTOs.UpdateUserDto;
import garbagegroup.cloud.DTOs.CreateUserDto;
import garbagegroup.cloud.DTOs.DTOConverter;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.jwt.JwtService;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;
import garbagegroup.cloud.model.User;
import garbagegroup.cloud.repository.IUserRepository;
import garbagegroup.cloud.service.serviceInterface.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class UserService implements IUserService {

    private final JwtService jwtService;
    private IUserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;



    @Autowired
    public UserService(JwtService jwtService, IUserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;

//        User user = new User("admin", "password", "admin", "municipality worker", "horsens");
//        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
//        IUserRepository.save(user);
    }

    /**
     * Fetches user details from the repository based on the provided username.
     *
     * @param username The username of the user to be fetched.
     * @return The User object corresponding to the provided username.
     * @throws RuntimeException If the user with the given username is not found in the repository.
     */
    @Override
    public User fetchUserByUsername(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user;
        } else {
            throw new RuntimeException("User with username " + username + " not found");
        }
    }

    /**
     * Updates user information based on the provided UpdateUserDto.
     * If the user is found by username, the information (password, fullname, region) will be updated and saved to the database.
     *
     * @param user The UpdateUserDto containing the updated user information.
     * @throws RuntimeException If the user with the given username is not found or if an unexpected exception occurs during the update process.
     */
    @Override
    public void updateUser(UpdateUserDto user) {
        try {
            User userToUpdate = userRepository.findByUsername(user.getUsername());
            if (userToUpdate != null) {
                userToUpdate.setPassword(this.passwordEncoder.encode(user.getPassword()));
                userToUpdate.setFullname(user.getFullname());
                userToUpdate.setRegion(user.getRegion());

                // Save the updated user to the database
                userRepository.save(userToUpdate);
        }
        } catch (Exception e) {
            throw new RuntimeException("User with username " + user.getUsername() + " not found");
        }
    }

    /**
        * Fetches all users from the database.
        *
        * @return A list of UserDto objects.
        * @throws RuntimeException If no users are found.
        */
    @Override
    public List<UserDto> fetchAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos= new ArrayList<>();
        if (users.isEmpty()) {
            throw new RuntimeException("No users found");
        }
        for (User user : users) {
            userDtos.add(DTOConverter.convertToUserDto(user));
        }
        return userDtos;
    }


    /**
     * Authenticates a user based on the provided UserDto credentials.
     *
     * @param request The UserDto containing the username and password for authentication.
     * @return An AuthenticationResponse containing the generated JWT token upon successful authentication.
     * @throws RuntimeException      If the user is not found after authentication.
     */
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

    /**
     * Converts UserDTO to User model object and encodes the password before saving to database
     * Checks if the user already exists
     * @param createUserDto
     * @return saved User
     */
    public User create(CreateUserDto createUserDto) {
        List<UserDto> users = fetchAllUsers();
        String requestedUsername = createUserDto.getUsername();
        for(UserDto userDto : users) {
            if (userDto.getUsername().equalsIgnoreCase(createUserDto.getUsername())) {
                throw new DuplicateKeyException("User with username '" + requestedUsername + "' already exists.");
            }
        }
        User user = DTOConverter.createUserDtoToUser(createUserDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if(user.getRole().equalsIgnoreCase("garbage collector")) {
            return userRepository.save(user);
        } else {
            throw new IllegalArgumentException("Only Garbage Collectors can be created");
        }
    }
}
