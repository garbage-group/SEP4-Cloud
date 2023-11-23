package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.dto.UserDto;
import garbagegroup.cloud.model.User;


public interface IUserService {

    User fetchUserByUsername(String username);

}
