package com.example.orderservice.dto;

import lombok.Data;

@Data
public class OrderDto {
  private String orderId;
  private String userId;
  private String productId;
  private Integer qty;
  private Integer unitPrice;
  private Integer totalPrice;
}
