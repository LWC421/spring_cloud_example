package com.example.userservice.vo;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class RequestUser {

  @NotNull(message = "")
  private String email;
  private String name;
  private String pwd;
}
