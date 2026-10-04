package com.payment.wallet_system.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.entity.WalletStatus;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.security.JwtService;
import com.payment.wallet_system.service.WalletService;

@WebMvcTest (WalletController.class)
@AutoConfigureMockMvc (addFilters = false)
public class WalletControllerTest {
    
    @MockitoBean 
    private  WalletService walletService;
    
    @MockitoBean 
    private UserRepository userRepository;
    
    @MockitoBean 
    private JwtService jwtService;
    
    @Autowired 
    private  MockMvc mockMvc;
    
   @WithMockUser(username="test@gmail.com")
   @Test 
    void shouldGetWalletSuccessfully() throws Exception{

        WalletResponse response=new  WalletResponse();
        response.setWalletNumber("WALLET-123");
        response.setBalance(new BigDecimal("500.00"));
        response.setStatus(WalletStatus.ACTIVE);

        when(walletService.getWallet(anyString()))
            .thenReturn(response);

        
        mockMvc.perform(
            get("/api/wallet")

        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.walletNumber").value("WALLET-123"))
        .andExpect(jsonPath("$.balance").value(500.00))
        .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @WithMockUser (username = "test@gmail.com")
    @Test 
    void shouldAddMoneySuccessfully() throws Exception{
       WalletResponse response=new  WalletResponse();
       response.setWalletNumber("WALLET-123");
       response.setBalance(new BigDecimal("1000.00"));
       response.setStatus(WalletStatus.ACTIVE);

       when(walletService.addMoney(
        anyString(), 
        any(AddMoneyRequest.class)
       )).thenReturn(response);

       mockMvc.perform(
        post("/api/wallet/add-money")
             .contentType(MediaType.APPLICATION_JSON)
             .content("""
                {
                "amount":500.00
                }
             """)
       )
       .andExpect(status().isOk())
       .andExpect(jsonPath("$.walletNumber").value("WALLET-123"))
       .andExpect(jsonPath("$.balance").value(1000.00))
       .andExpect(jsonPath("$.status").value("ACTIVE"));
    }
    
    @Test
    void shouldFailWhenAddMoneyAmountIsInvalid() throws Exception{
       
         mockMvc.perform(
            post("/api/wallet/add-money")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "amount":0
                    }
                """)
         )
         .andExpect(status().isBadRequest());
    }
    
    @Test 
    void shouldFailWhenTransferRequestIsInvalid() throws Exception{
       
        mockMvc.perform(
            post("/api/wallet/transfer")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content("""
                    {
                    "receiverEmail":"receiver@gmail.com",
                    "amount":0
                    }
                 """)
        )
        .andExpect(status().isBadRequest());
    }
}
