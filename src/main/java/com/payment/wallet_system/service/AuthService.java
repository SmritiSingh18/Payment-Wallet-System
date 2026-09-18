package com.payment.wallet_system.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.LoginRequest;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.exception.InvalidCredentials;
import com.payment.wallet_system.respository.UserRepository;

@Service 
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public  AuthService (UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
    }
    public User login(LoginRequest request){
        Optional<User> userOptional=userRepository.findByEmail(request.getEmail());
        if(userOptional.isEmpty()){
            throw new InvalidCredentials("Invalid email");
        }

        User user=userOptional.get();

        boolean passwordMatches=
        passwordEncoder.matches(
            request.getPassword(),
            user.getPassword()
        );
        if(!passwordMatches){
            throw new InvalidCredentials("Invalid Password");
    }
    return  user;
    
   }
}
