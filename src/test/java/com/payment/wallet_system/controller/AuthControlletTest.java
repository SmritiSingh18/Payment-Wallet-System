package com.payment.wallet_system.controller;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import com.payment.wallet_system.dto.LoginRequest;
import com.payment.wallet_system.dto.LoginResponse;
import com.payment.wallet_system.entity.Role;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.security.JwtService;
import com.payment.wallet_system.service.AuthService;

@WebMvcTest (AuthControler.class)
@AutoConfigureMockMvc(addFilters = false)  
public class AuthControlletTest {

@MockitoBean 
private  UserRepository userRepository;

@MockitoBean 
private AuthService authService;

@MockitoBean 
private  JwtService jwtService;

@Autowired 
private MockMvc mockMvc;
   
@Test   
void  shouldLoginSuccessfully() throws Exception{
    LoginRequest request=new  LoginRequest();
    request.setEmail("test@gmail.com");
    request.setPassword("password123");

    LoginResponse response=new LoginResponse();
    response.setId(1L);
    response.setName("Test User");
    response.setEmail("test@gmail.com");
    response.setRole(Role.USER);
    response.setToken("fake-jwt-token");

    when(authService.login(any(LoginRequest.class)))
        .thenReturn(response);
    
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                  {
                "email":"test@gmail.com",
                "password":"password123"
                }
            """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.name").value("Test User"))
        .andExpect(jsonPath("$.email").value("test@gmail.com"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    
  }
}
