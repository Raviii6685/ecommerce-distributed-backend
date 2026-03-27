package zatribune.spring.ex_mongodb_docker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zatribune.spring.ex_mongodb_docker.entities.Product;
import zatribune.spring.ex_mongodb_docker.entities.User;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findAllByUser(User user);
}
