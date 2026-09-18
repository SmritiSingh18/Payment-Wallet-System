package com.payment.wallet_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration 
public class SecurityConfig {

    @Bean 
      public  PasswordEncoder passwordEncoder(){ // interace
      return new  BCryptPasswordEncoder();// actual implementation
    }
    
    @Bean 
    public  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/users","/api/auth/login").permitAll()
            .anyRequest().authenticated()

          );

        return  http.build();
    }
    
}
