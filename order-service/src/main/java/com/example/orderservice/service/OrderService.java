package com.example.orderservice.service;

import com.example.orderservice.dto.OrderDto;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
  OrderDto createOrder();
}
