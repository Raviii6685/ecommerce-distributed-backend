package zatribune.spring.ex_mongodb_docker.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import zatribune.spring.ex_mongodb_docker.entities.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    Page<Order> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
}
