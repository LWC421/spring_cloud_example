package com.example.userservice.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class WebSecurity extends WebSecurityConfigurerAdapter {
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable();
    
    http.authorizeRequests()
        .antMatchers("/users/**").permitAll();
    
    http.authorizeRequests()
            .antMatchers("/actuator/**").permitAll();
    
    http.headers().frameOptions().disable();  //h2-console에서 오류가 나는 것 방지
  }
}
