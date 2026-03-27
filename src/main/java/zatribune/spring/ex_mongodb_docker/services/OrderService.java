package zatribune.spring.ex_mongodb_docker.services;

import zatribune.spring.ex_mongodb_docker.dto.OrderRequest;
import zatribune.spring.ex_mongodb_docker.entities.Order;
import zatribune.spring.ex_mongodb_docker.entities.OrderStatus;

import java.util.List;

public interface OrderService {
    
    Order placeOrder(String userId, OrderRequest request);
    
    List<Order> getUserOrders(String userId);
    
    Order getOrderById(String orderId);
    
    List<Order> getAllOrders();
    
    Order updateOrderStatus(String orderId, OrderStatus status);
}
