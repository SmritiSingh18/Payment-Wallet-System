package com.payment.wallet_system.security;

import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.respository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository; 
    private final JwtService jwtService;
    public JwtAuthenticationFilter(UserRepository userRepository,JwtService jwtService){
        this.userRepository=userRepository;
        this.jwtService=jwtService;
    }
    @Override 
    protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain filterChain) throws ServletException,IOException{
       String authHeader=request.getHeader("Authorization");
      
       System.out.println("AUTH HEADER: " + authHeader);
       if(authHeader== null || !authHeader.startsWith("Bearer ")){
        filterChain.doFilter(request, response);
       return ;
    }
    String token=authHeader.substring(7);
    String email=jwtService.extractEmail(token);
    User user=userRepository.findByEmail(email)
                            .orElse(null);

    if(user!= null && jwtService.isTokenValid(token, user)){
        System.out.println("AUTHENTICATED USER: " + user.getEmail());
        UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(user.getEmail(),null,
            java.util.List.of(new  SimpleGrantedAuthority("ROLE_"+user.getRole().name()))
        );

    SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
                       
    }
    filterChain.doFilter(request, response);
}               
}


