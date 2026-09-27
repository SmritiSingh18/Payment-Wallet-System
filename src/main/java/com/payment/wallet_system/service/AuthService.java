package com.payment.wallet_system.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.payment.wallet_system.dto.LoginRequest;
import com.payment.wallet_system.dto.LoginResponse;
import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.exception.InvalidCredentials;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.security.JwtService;

@Service 
public class AuthService {

    private final AuditLogService auditLogService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public  AuthService (UserRepository userRepository,PasswordEncoder passwordEncoder,JwtService jwtService, AuditLogService auditLogService){
        this.userRepository=userRepository;
        this.passwordEncoder=passwordEncoder;
        this.jwtService=jwtService;
        this.auditLogService = auditLogService;
    }
    public LoginResponse login(LoginRequest request){
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
         
          auditLogService.log(user.getId(),AuditEventType.USER_LOGIN,"User login Succesful" ,null);
        
        if(!passwordMatches){
            throw new InvalidCredentials("Invalid Password");

      
       
    }
       String token =jwtService.generateToken(user);
       LoginResponse response=new LoginResponse();
       response.setId(user.getId());
       response.setName(user.getName());
       response.setEmail(user.getEmail());
       response.setRole(user.getRole());
       response.setToken(token);
    return  response;
    
   }
}
