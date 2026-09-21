package com.payment.wallet_system.config;

import com.payment.wallet_system.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.payment.wallet_system.security.JwtAuthenticationFilter;

@Configuration 
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  public  SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter){
   this.jwtAuthenticationFilter=jwtAuthenticationFilter;
  }
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

          )
          .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return  http.build();
    }
    
}
