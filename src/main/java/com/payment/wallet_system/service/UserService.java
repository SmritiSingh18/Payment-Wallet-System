package com.payment.wallet_system.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.RegisterRequest;
import com.payment.wallet_system.dto.RegisterResponse;
import com.payment.wallet_system.entity.Role;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.entity.WalletStatus;
import com.payment.wallet_system.exception.DuplicateResourceException;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

@Service 
public class UserService {
    private  final UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private  final WalletRepository walletRepository;
    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder,WalletRepository walletRepository){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.walletRepository=walletRepository;
    }

    public RegisterResponse resgisterUser(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new DuplicateResourceException("Email already exists");
        }
        User user=new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        User savedUser=userRepository.save(user);
        
        Wallet wallet=new Wallet();
        wallet.setWalletNumber("Wallet_"+System.currentTimeMillis());
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setUser(savedUser);
        walletRepository.save(wallet);

        

        RegisterResponse response=new RegisterResponse();
        response.setId(savedUser.getId());
        response.setName(savedUser.getName());
        response.setEmail(savedUser.getEmail());
        response.setRole(savedUser.getRole());
        response.setCreatedAt(savedUser.getCreatedAt());

        return  response;
    }
}
