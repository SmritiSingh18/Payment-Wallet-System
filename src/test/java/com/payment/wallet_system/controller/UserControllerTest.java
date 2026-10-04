package com.payment.wallet_system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.payment.wallet_system.dto.RegisterRequest;
import com.payment.wallet_system.dto.RegisterResponse;
import com.payment.wallet_system.entity.Role;
import com.payment.wallet_system.exception.DuplicateResourceException;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.security.JwtService;
import com.payment.wallet_system.service.UserService;

@WebMvcTest (UserController.class)
@AutoConfigureMockMvc(addFilters=false)
public class UserControllerTest {
    
    @MockitoBean 
    private UserService userService;
    
    @MockitoBean 
    private  JwtService jwtService;
    
    @MockitoBean 
    private  UserRepository userRepository;
    
    @Autowired 
    private MockMvc mockMvc;
    
    @Test 
    void shouldRegisterUserSuccessfully() throws Exception{
        RegisterResponse response=new  RegisterResponse();
        response.setId(1L);
        response.setName("Test User");
        response.setEmail("test@gmail.com");
        response.setRole(Role.USER);

        when(userService.resgisterUser(any(RegisterRequest.class)))
            .thenReturn(response);

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name":"Test User",
                    "email":"test@gmail.com",
                    "password":"password123"
                    }
                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.role").value("USER"));
    }
    
    @Test 
    void shouldFailWhenEmailAlreadyExists() throws Exception{
         when(userService.resgisterUser(any(RegisterRequest.class)))
             .thenThrow(new DuplicateResourceException("Email already exists"));

        mockMvc.perform(
            post("/api/users")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content("""
                    {
                    "name":"Test User",
                    "email":"test@gmail.com",
                    "password":"password123"
                    }
                 """)
        )
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Email already exists"));
    }
    
    @Test 
    void shouldFailWhenRegsitrationRequestIsInvalid() throws Exception{
         
        mockMvc.perform(
            post("/api/users")
             .contentType(MediaType.APPLICATION_JSON)
             .content("""
                {
                "name":"",
                "email":"invalid-email",
                "password":""
             }
             """)
        )
        .andExpect(status().isBadRequest());
    }
}
