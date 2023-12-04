package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.DTOs.UpdateUserDto;
import garbagegroup.cloud.model.User;


public interface IUserService {

    User fetchUserByUsername(String username);

    void updateUser(UpdateUserDto user);

}
