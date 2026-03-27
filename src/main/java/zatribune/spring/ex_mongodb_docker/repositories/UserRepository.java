package zatribune.spring.ex_mongodb_docker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zatribune.spring.ex_mongodb_docker.entities.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
