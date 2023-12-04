package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.CreateUserDto;
import garbagegroup.cloud.model.User;


public interface IUserService {

    User fetchUserByUsername(String username);
    User create(CreateUserDto createUserDto);

}
