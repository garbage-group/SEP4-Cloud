package garbagegroup.cloud.service.serviceImplementation;

import garbagegroup.cloud.DTOs.*;
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

import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.List;


@Service
public class UserService implements IUserService {
    private final JwtService jwtService;
    private final IUserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(JwtService jwtService, IUserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
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
            // Creating a new User object to send with a placeholder password
            User userToSend = new User();
            userToSend.setUsername(user.getUsername());
            userToSend.setRole(user.getRole());
            userToSend.setFullname(user.getFullname());
            userToSend.setRegion(user.getRegion());
            userToSend.setPassword("********"); // Placeholder password

            return userToSend;
        } else {
            throw new NoSuchElementException("User with username " + username + " not found");
        }
    }


    /**
     * Deletes Garbage collector in the DB by their username
     * Only allows to delete users whose role is "garbage collector"
     * @param username
     */
    @Override
    public boolean deleteByUsername(String username) {
        if (userRepository.existsById(username)) {
            if (userRepository.findByUsername(username).getRole().equalsIgnoreCase("municipality worker"))
                throw new IllegalArgumentException("You may only delete garbage collectors! No other roles.");
            userRepository.deleteById(username);
            return true;
        } else throw new NoSuchElementException("User with username '" + username + "' not found");
    }

    /**
     * Updates user information based on the provided UpdateUserDto.
     * If the user is found by username, the information (password, fullname, region) will be updated and saved to the database.
     *
     * @param user The UpdateUserDto containing the updated user information.
     * @return
     * @throws RuntimeException If the user with the given username is not found or if an unexpected exception occurs during the update process.
     */
    @Override
    public boolean updateUser(UpdateUserDto user) {
        try {
            User userToUpdate = userRepository.findByUsername(user.getUsername());
            if (userToUpdate != null) {
                // Check if the received password is different from the stored password
                if (!user.getPassword().equals("********")) {
                    userToUpdate.setPassword(this.passwordEncoder.encode(user.getPassword()));
                }
                // Update other fields
                userToUpdate.setFullname(user.getFullname());
                userToUpdate.setRegion(user.getRegion());
                // Save the updated user to the database
                userRepository.save(userToUpdate);
                return true;
            }
        } catch (Exception e) {
            System.err.println("Error updating the User with username: " + user.getUsername() + e.getMessage());
            return false;
        }
        return false;
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