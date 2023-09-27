package com.example.userservice.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class RequestUser {

  @NotNull(message = "Email is Null")
  @Size(min = 2, message = "Email more then 2")
  private String email;
  
  @NotNull(message = "Name is Null")
  @Size(min = 2, message = "Name more then 2")
  private String name;
  
  @NotNull(message = "Password is Null")
  @Size(min = 8, message = "Password more then 8")
  private String pwd;
}
