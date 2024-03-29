package com.example.catalogservice.messagequeue;

import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.jpa.CatalogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class KafkaConsumer {
  
  CatalogRepository catalogRepository;
  
  @Autowired
  public KafkaConsumer(CatalogRepository catalogRepository){
    this.catalogRepository = catalogRepository;
  }
  
  @KafkaListener(topics = "example-catalog-topic")
  public void updateQty(String kafkaMessage){
    //토픽에서 카프카의 메시지를 가져오면 어떻게 처리할 지 정할 수 있다
    Map<Object, Object> map = new HashMap<>();
    ObjectMapper mapper = new ObjectMapper();
    try{
      map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
      
    } catch(JsonProcessingException e){
      e.printStackTrace();
    }
    
    CatalogEntity entity = catalogRepository.findByProductId((String)map.get("productId"));

    if(entity != null){
      entity.setStock(entity.getStock() - (Integer)map.get("qty"));
      catalogRepository.save(entity);
    }
    
  }
}
