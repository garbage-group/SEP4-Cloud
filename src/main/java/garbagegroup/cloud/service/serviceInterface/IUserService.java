package garbagegroup.cloud.service.serviceInterface;

import garbagegroup.cloud.model.User;


public interface IUserService {

    User fetchUserByUsername(String username);

}
