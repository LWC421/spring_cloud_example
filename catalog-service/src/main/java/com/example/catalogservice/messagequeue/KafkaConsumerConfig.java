package com.example.catalogservice.messagequeue;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {
  
  //실제 카프카 관련 설정을 해야한다
  @Bean
  public ConsumerFactory<String, String> oconsumerFactory(){
    Map<String, Object> properties = new HashMap<>();
    
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "127.0.0.1:9092");
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumerGroupId");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    
    return new DefaultKafkaConsumerFactory<>(properties);
  }
  
  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(){
    ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory
        = new ConcurrentKafkaListenerContainerFactory<>();
    
    kafkaListenerContainerFactory.setConsumerFactory(oconsumerFactory());
    
    return kafkaListenerContainerFactory;
  }
}
