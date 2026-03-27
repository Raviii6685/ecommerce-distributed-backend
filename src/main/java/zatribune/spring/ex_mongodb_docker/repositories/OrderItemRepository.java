package zatribune.spring.ex_mongodb_docker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import zatribune.spring.ex_mongodb_docker.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
}
