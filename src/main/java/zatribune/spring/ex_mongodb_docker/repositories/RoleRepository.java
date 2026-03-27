package zatribune.spring.ex_mongodb_docker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zatribune.spring.ex_mongodb_docker.entities.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {
    Optional<Role> findByName(String name);
}
