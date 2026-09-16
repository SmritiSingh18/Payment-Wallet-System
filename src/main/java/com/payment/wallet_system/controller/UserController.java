package com.payment.wallet_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.wallet_system.dto.RegisterRequest;
import com.payment.wallet_system.dto.RegisterResponse;
import com.payment.wallet_system.service.UserService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequestMapping ("/api/users")
public class UserController {
    private  final UserService userService;
    public  UserController(UserService userService){
        this.userService=userService;
    }
    @PostMapping
    public RegisterResponse registerUser(@RequestBody @Valid  RegisterRequest request) {
        
        return userService.resgisterUser(request);
    }
    
}
