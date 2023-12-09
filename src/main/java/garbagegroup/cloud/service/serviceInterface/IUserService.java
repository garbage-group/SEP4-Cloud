package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.*;
import garbagegroup.cloud.jwt.auth.AuthenticationResponse;
import garbagegroup.cloud.model.User;

import java.util.List;

public interface IUserService {
    User fetchUserByUsername(String username);
    User create(CreateUserDto createUserDto);
    List<UserDto> fetchAllUsers();
    boolean updateUser(UpdateUserDto user);
    boolean deleteByUsername(String username);

    AuthenticationResponse authenticate(UserDto request);
}
