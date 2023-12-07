package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.UpdateUserDto;
import garbagegroup.cloud.DTOs.CreateUserDto;
import garbagegroup.cloud.DTOs.UserDto;
import garbagegroup.cloud.model.User;

import java.util.List;


public interface IUserService {
    User fetchUserByUsername(String username);
    User create(CreateUserDto createUserDto);
    List<UserDto> fetchAllUsers();
    boolean updateUser(UpdateUserDto user);
    boolean deleteByUsername(String username);
}
