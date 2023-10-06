package com.example.orderservice.messagequeue;

import com.example.orderservice.dto.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.discovery.converters.Auto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class OrderProducer {

  private KafkaTemplate<String, String> kafkaTemplate;
  
  //DB에 들어가야되는 필드에 대한 정보를 적어놓자
  List<Field> fields = Arrays.asList(new Field("string", true, "order_id"),
      new Field("string", true, "user_id"),
      new Field("string", true, "product_id"),
      new Field("int32", true, "qty"),
      new Field("int32", true, "unit_price"),
      new Field("int3", true, "total_price")
      );
  Schema schema = Schema.builder()
      .type("struct")
      .fields(fields)
      .optional(false)
      .name("orders")
      .build();
  
  @Autowired
  public OrderProducer(KafkaTemplate<String, String> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }
  
  public OrderDto send(String topic, OrderDto orderDto){
    Payload payload = Payload.builder()
        .order_id(orderDto.getOrderId())
        .user_id(orderDto.getUserId())
        .product_id(orderDto.getProductId())
        .qty(orderDto.getQty())
        .unit_price(orderDto.getUnitPrice())
        .total_price(orderDto.getTotalPrice())
        .build();
    
    // 카프카에 전달할 정보를 스키마까지 포함하자
    KafkaOrderDto kafkaOrderDto = new KafkaOrderDto(schema, payload);
    
    ObjectMapper mapper = new ObjectMapper();
    String jsonInString = "";
    try{
      jsonInString = mapper.writeValueAsString(kafkaOrderDto);
    }
    catch(JsonProcessingException e){
      e.printStackTrace();
    }
    
    kafkaTemplate.send(topic, jsonInString);
    
    return orderDto;
  }
}
