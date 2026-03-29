package zatribune.spring.ex_mongodb_docker.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import zatribune.spring.ex_mongodb_docker.dto.OrderRequest;
import zatribune.spring.ex_mongodb_docker.entities.Order;
import zatribune.spring.ex_mongodb_docker.entities.OrderStatus;



public interface OrderService {

    Order placeOrder(String userId, OrderRequest request);

    Page<Order> getUserOrders(String userId, Pageable pageable);

    Order getOrderById(String orderId);



    Page<Order> getAllOrders(Pageable pageable);

    Order updateOrderStatus(String orderId, OrderStatus status);
}
