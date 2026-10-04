package com.payment.wallet_system.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.payment.wallet_system.dto.TransactionResponse;
import com.payment.wallet_system.entity.TransactionStatus;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.security.JwtService;
import com.payment.wallet_system.service.TransactionService;

@WebMvcTest (TransactionController.class)
@AutoConfigureMockMvc (addFilters = false)
public class TransactionControllerTest {
    
    @MockitoBean 
    private  TransactionService transactionService;
    
    @MockitoBean 
    private  UserRepository userRepository;
    
    @MockitoBean 
    private  JwtService jwtService;
    
    @Autowired 
    private MockMvc mockMvc;

    @WithMockUser (username = "test@gmail.com")
    @Test 
    void shouldGetTransactionHistorySuccessfully() throws Exception{
        TransactionResponse transaction =new TransactionResponse();
        transaction.setTransactionId("TXN-123");
        transaction.setSenderWalletNumber("WALLET-001");
        transaction.setReceiverWalletNumber("WALLET-002");
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setCreatedAt(LocalDateTime.now());

        when(transactionService.getTransactionDetails("test@gmail.com"))
             .thenReturn(List.of(transaction));

        mockMvc.perform(
            get("/api/transactions")
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].transactionId").value("TXN-123"))
        .andExpect(jsonPath("$[0].senderWalletNumber").value("WALLET-001"))
        .andExpect(jsonPath("$[0].receiverWalletNumber").value("WALLET-002"))
        .andExpect(jsonPath("$[0].amount").value(500.00))
        .andExpect(jsonPath("$[0].status").value("SUCCESS"));
    }
    
    @WithMockUser (username = "test@gmail.com")
    @Test 
    void shouldReturnEmptyTransactionHistory() throws Exception{
        when(transactionService.getTransactionDetails("test@gmail.com"))
            .thenReturn(List.of());

        mockMvc.perform(
            get("/api/transactions")

        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty());
    }
}
