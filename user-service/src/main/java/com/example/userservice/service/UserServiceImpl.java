package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService{
  
  private UserRepository userRepository;
  private BCryptPasswordEncoder passwordEncoder;
  
  private OrderServiceClient orderServiceClient;
  
  private CircuitBreakerFactory circuitBreakerFactory;
  
  
  public UserServiceImpl(UserRepository userRepository,
                         BCryptPasswordEncoder passwordEncoder,
                         OrderServiceClient orderServiceClient,
                         CircuitBreakerFactory circuitBreakerFactory){
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.orderServiceClient = orderServiceClient;
    this.circuitBreakerFactory = circuitBreakerFactory;
  }
  
  @Override
  public UserDto createUser(UserDto userDto) {
    userDto.setUserId(UUID.randomUUID().toString());
    
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    
    UserEntity userEntity = mapper.map(userDto, UserEntity.class);
    userEntity.setEncryptedPwd(passwordEncoder.encode(userDto.getPwd()));
    
    userRepository.save(userEntity);
    
    return mapper.map(userEntity, UserDto.class);
  }
  
  @Override
  public UserDto getUserByUserId(String userId) {
    UserEntity userEntity = userRepository.findByUserId(userId);
    
    if(userEntity == null){
      throw new UsernameNotFoundException("User Not Found");
    }
    UserDto userDto = new ModelMapper().map(userEntity, UserDto.class);
    
    log.info("Before call order microservice");
    CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
    List<ResponseOrder> orderList = (List<ResponseOrder>) circuitBreaker.run( () -> orderServiceClient.getOrders(userId),
      throwable -> new ArrayList<ResponseOrder>());
    log.info("After call order microservice");
    
    userDto.setOrders(orderList);
    
    return userDto;
  }
  
  @Override
  public Iterable<UserEntity> getUserByAll() {
    return userRepository.findAll();
  }
}
