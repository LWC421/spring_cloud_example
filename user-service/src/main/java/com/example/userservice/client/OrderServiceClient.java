package com.example.userservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="order-service")
public interface OrderServiceClient {
  
  @GetMapping("/order-service/{userId}/orders")
  String getOrders(@PathVariable String userId);
  
}
