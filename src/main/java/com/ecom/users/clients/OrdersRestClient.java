package com.ecom.users.clients;

import com.ecom.users.model.Order;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "orders-service", url = "http://localhost:8095/api")
public interface OrdersRestClient {

    @PostMapping("/_internal/order-user")
    @CircuitBreaker(name="orders")
    Order createOrder(@RequestHeader("Authorization") String authorization, @RequestBody Order order);

}
