package com.payment.wallet_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.payment.wallet_system.dto.LoginRequest;
import com.payment.wallet_system.dto.LoginResponse;
import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.Role;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.exception.InvalidCredentials;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.security.JwtService;

@ExtendWith (MockitoExtension.class)
public class AuthServiceTest {

    @Mock 
    private  UserRepository userRepository;
    
    @Mock 
    private  PasswordEncoder passwordEncoder;
     
    @Mock 
    private AuditLogService auditLogService;
    
    @Mock 
    private JwtService jwtService;
    
    @InjectMocks 
    private  AuthService authService;
    
    @Test 
    void shouldLoginSuccessfully(){
       User user=new User();
       user.setId(1L);
       user.setName("Test User");
       user.setEmail("test@gmail.com");
       user.setPassword("hashedPassword");
       user.setRole(Role.USER);

       LoginRequest request=new  LoginRequest();
       request.setEmail("test@gmail.com");
       request.setPassword("password123");

       when(userRepository.findByEmail("test@gmail.com"))
           .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashedPassword"))
            .thenReturn(true);

        when(jwtService.generateToken(user))
            .thenReturn("fake-jwt-token");
        
        LoginResponse response=authService.login(request);

        assertNotNull(response);
        assertEquals(1L  , response.getId());
        assertEquals("Test User", response.getName());
        assertEquals("test@gmail.com", response.getEmail());
        assertEquals(Role.USER, response.getRole());
        assertEquals("fake-jwt-token", response.getToken());

        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtService).generateToken(user);
        auditLogService.log(
            user.getId(),
             AuditEventType.USER_LOGIN,
              "User login Successful",
               null);

        verify(auditLogService).log(
            user.getId(),
             AuditEventType.USER_LOGIN, 
             "User login Successful",
              null);
    }
    
    @Test 
    void shouldFailWhenUserNotFound(){
        LoginRequest request=new  LoginRequest();
        request.setEmail("unknown@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("unknown@gmail.com"))
            .thenReturn(Optional.empty());

        InvalidCredentials exception=assertThrows(
            InvalidCredentials.class,
             ()-> authService.login(request));

        assertEquals("Invalid email", exception.getMessage());
        verify(passwordEncoder,never()).matches(anyString(), anyString());
    }

    @Test 
    void shouldFailWhenPasswordIsIncorrect(){
        User user=new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@gmail.com");
        user.setPassword("hashedPassword");
        user.setRole(Role.USER);

        LoginRequest request=new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("wrongPassword");

        when(userRepository.findByEmail("test@gmail.com"))
             .thenReturn(Optional.of(user));
        
        when(passwordEncoder.matches("wrongPassword", "hashedPassword"))
             .thenReturn(false);

        InvalidCredentials exception=assertThrows(
            InvalidCredentials.class,()->authService.login(request));

        assertEquals("Invalid Password", exception.getMessage());
        verify(jwtService,never()).generateToken(any(User.class));
    }
    
}
