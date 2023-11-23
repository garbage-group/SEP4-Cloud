package garbagegroup.cloud.repository;


import garbagegroup.cloud.dto.UserDto;
import garbagegroup.cloud.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, String> {

    User findByUsername(String username);
}
