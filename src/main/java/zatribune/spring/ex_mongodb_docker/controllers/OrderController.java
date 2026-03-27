package zatribune.spring.ex_mongodb_docker.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import zatribune.spring.ex_mongodb_docker.dto.OrderRequest;
import zatribune.spring.ex_mongodb_docker.entities.Order;
import zatribune.spring.ex_mongodb_docker.entities.OrderStatus;
import zatribune.spring.ex_mongodb_docker.entities.User;
import zatribune.spring.ex_mongodb_docker.services.OrderService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public Order createOrder(@AuthenticationPrincipal User user, @Valid @RequestBody OrderRequest request) {
        // We pass the whole User entity from AuthenticationPrincipal but only use the ID
        return orderService.placeOrder(user.getId(), request);
    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated()")
    public List<Order> getMyOrders(@AuthenticationPrincipal User user) {
        return orderService.getUserOrders(user.getId());
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated()")
    public Order getOrderById(@PathVariable String id, @AuthenticationPrincipal User user) {
        Order order = orderService.getOrderById(id);
        
        // Security check: Only the owner or an ADMIN can view the order
        boolean isAdmin = user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"));
        if (!order.getUser().getId().equals(user.getId()) && !isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Access is denied");
        }
        
        return order;
    }

    @PutMapping("/orders/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public Order cancelOrder(@PathVariable String id, @AuthenticationPrincipal User user) {
        Order order = orderService.getOrderById(id);
        if (!order.getUser().getId().equals(user.getId())) {
             throw new org.springframework.security.access.AccessDeniedException("Access is denied");
        }
        return orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public Order updateOrderStatus(@PathVariable String id, @RequestParam OrderStatus status) {
        return orderService.updateOrderStatus(id, status);
    }
}
