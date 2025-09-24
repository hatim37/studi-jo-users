package com.ecom.users.clients;

import com.ecom.users.enums.OrderStatus;
import com.ecom.users.model.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "orders-service", url = "${orders.service.url}")
public interface OrdersRestClient {

    @PostMapping("/_internal/order-user")
    @CircuitBreaker(name = "orders", fallbackMethod = "createOrder")
    ResponseEntity<Order> createOrder(@RequestHeader("Authorization") String authorization, @RequestBody Order order);

    @PostMapping("/_internal/orders/sync")
    @CircuitBreaker(name = "orders-sync", fallbackMethod = "synchronizeOrders")
    void synchronizeOrders(@RequestHeader("Authorization") String token);

    default void synchronizeOrders(String token, Throwable throwable) {}

    default ResponseEntity<Order> createOrder(String authorization, Order order, Throwable throwable) {
        Order orderFallback = new Order();
        order.setUserId(null);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(orderFallback);
    }


}
