package zatribune.spring.ex_mongodb_docker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zatribune.spring.ex_mongodb_docker.dto.OrderRequest;
import zatribune.spring.ex_mongodb_docker.entities.Order;
import zatribune.spring.ex_mongodb_docker.entities.OrderStatus;

import java.util.List;

public interface OrderService {

    Order placeOrder(String userId, OrderRequest request);

    List<Order> getUserOrders(String userId);

    Page<Order> getUserOrders(String userId, Pageable pageable);

    Order getOrderById(String orderId);

    List<Order> getAllOrders();

    Page<Order> getAllOrders(Pageable pageable);

    Order updateOrderStatus(String orderId, OrderStatus status);
}
