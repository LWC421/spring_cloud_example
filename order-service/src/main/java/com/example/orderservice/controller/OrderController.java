package com.example.orderservice.controller;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order-service")
public class OrderController {
  
  Environment env;
  
  public OrderController(Environment env){
    this.env = env;
  }
  
  @GetMapping("/health_check")
  public String status(){
    return String.format("Catalog Service PORT : %s", env.getProperty("local.server.port"));
  }
  
}
