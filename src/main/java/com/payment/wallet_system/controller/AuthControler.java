package com.payment.wallet_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.wallet_system.dto.LoginRequest;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/auth")
public class AuthControler {
    private final AuthService authService;
    public  AuthControler(AuthService authService){
        this.authService=authService;
    }
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody @Valid LoginRequest request) {
        User user=authService.login(request);
        
        return ResponseEntity.ok(user);
    }
    

}
