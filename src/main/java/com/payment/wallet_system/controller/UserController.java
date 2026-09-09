package com.payment.wallet_system.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.service.UserService;
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
    public User registerUser(@RequestBody User user) {
        
        return userService.resgisterUser(user);
    }
    
}
