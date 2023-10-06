package com.example.orderservice.controller;

import com.example.orderservice.dto.OrderDto;
import com.example.orderservice.messagequeue.KafkaProducer;
import com.example.orderservice.service.OrderService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order-service")
public class OrderController {
  
  Environment env;
  OrderService orderService;
  KafkaProducer kafkaProducer;
  
  public OrderController(Environment env,
                         OrderService orderService,
                         KafkaProducer kafkaProducer) {
    this.env = env;
    this.orderService = orderService;
    this.kafkaProducer = kafkaProducer;
  }
  
  @GetMapping("/health_check")
  public String status(){
    return String.format("Catalog Service PORT : %s", env.getProperty("local.server.port"));
  }

  @PostMapping("/{userId}/orders")
  public ResponseEntity createOrder(@PathVariable("userId") String userId){
    
    /* 데이터 만들기 생략 */
    
    OrderDto orderDto = orderService.createOrder();
    
    
    kafkaProducer.send("example-catalog-topic", orderDto);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(null);
  }
  
  @GetMapping("/{userId}/orders")
  public ResponseEntity<String> getOrder(@PathVariable("userId") String userId){
    
    return ResponseEntity.ok(userId);
  }
  
}

