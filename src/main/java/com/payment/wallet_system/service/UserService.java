package com.payment.wallet_system.service;

import org.springframework.stereotype.Service;

import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.respository.UserRepository;

@Service 
public class UserService {
    private  final UserRepository userRepository;
    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public User resgisterUser(User user){
        if(userRepository.existsByEmail(user.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        return  userRepository.save(user);
    }
}
