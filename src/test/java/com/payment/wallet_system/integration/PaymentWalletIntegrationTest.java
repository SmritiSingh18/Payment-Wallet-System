package com.payment.wallet_system.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.wallet_system.entity.Transaction;
import com.payment.wallet_system.entity.TransactionStatus;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

@SpringBootTest 
@AutoConfigureMockMvc 
public class PaymentWalletIntegrationTest {
     
    @Autowired 
    private  WalletRepository walletRepository;
    
    @Autowired 
    private  UserRepository userRepository;
    
    @Autowired 
    private TransactionRepository transactionRepository;
    
    @Autowired 
    private  MockMvc mockMvc;

    @Test 
    void contextLoads(){
    }
    
    @Test 
    void shouldRegisterUserSuccessfully() throws Exception{
       
       String email="integration-"+UUID.randomUUID()+"@gmail.com";

       mockMvc.perform(
        post("/api/users")
             .contentType(MediaType.APPLICATION_JSON)
             .content("""
                {
                "name":"Integration User",
                "email":"%s",
                "password":"password123"
                }
             """.formatted(email))
       )
       .andExpect(status().isOk());
       
       User user=userRepository.findByEmail(email)
               .orElseThrow();

        Wallet wallet=walletRepository.findByUser(user)
         .orElseThrow();

        assertEquals(email, user.getEmail());
        assertNotNull(wallet);
    }
    
    @Test 
    void shouldLoginSuccessfully() throws Exception{
       
        String email="login-"+UUID.randomUUID()+"@gmail.com";
        String password="password123";

        mockMvc.perform(
            post("/api/users")
                 .contentType(MediaType.APPLICATION_JSON)
                 .content("""
                    {
                    "name":"Login User",
                    "email":"%s",
                    "password":"%s"
                    }
                 """.formatted(email,password))

                )
                .andExpect(status().isOk());

                mockMvc.perform(
                    post("/api/auth/login")
                         .contentType(MediaType.APPLICATION_JSON)
                         .content("""
                            {
                            "email":"%s",
                            "password":"%s"
                            }
                         """.formatted(email,password))
                
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }
    
    @Test 
    void shouldAddMoneySuccessfully() throws Exception{
       String email="wallet-"+UUID.randomUUID()+"@gmail.com";
       String password="password123";

       mockMvc.perform(
        post("/api/users")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
            "name":"Waallet user",
            "email":"%s",
            "password":"%s"
            }
        """.formatted(email,password))
       )
       .andExpect(status().isOk());
    

    String loginResponse=mockMvc.perform(
        post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "email":"%s",
                "password":"%s"
                }
            """.formatted(email,password))
    )
    .andExpect(status().isOk())
    .andReturn()
    .getResponse()
    .getContentAsString();


      ObjectMapper objectMapper=new  ObjectMapper();
      JsonNode json=objectMapper.readTree(loginResponse);
      String token=json.get("token").asText();
      System.out.println("Token Created "+!token.isEmpty());


    mockMvc.perform(
        post("/api/wallet/add-money")
             .header("Authorization","Bearer " +token)
             .contentType(MediaType.APPLICATION_JSON)
             .content("""
                     {
                        "amount":1000.00
                     }
                     """)
    )
    .andDo(result -> {
        System.out.println("Test Header "+
        result.getRequest().getHeader("Authorization"));
    })
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.balance").value(1000.00));
    }
    
    @Test 
    void shouldTransferMoneySuccessfully() throws Exception{
      
        String senderEmail="sender-"+UUID.randomUUID()+"@gmail.com";
        String receiverEmail="receiver-"+UUID.randomUUID()+"@gmail.com";
        String password="password123";

        mockMvc.perform(
            post("/api/users")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                {
                "name":"Sender",
                "email":"%s",
                "password":"%s"
                }
              """.formatted(senderEmail,password))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
            post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "name":"Receiver",
                    "email":"%s",
                    "password":"%s"
                    }
                """.formatted(receiverEmail,password))
        )
        .andExpect(status().isOk());

        String senderResponse=mockMvc.perform(
            post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "email":"%s",
                "password":"%s"
                }
            """.formatted(senderEmail,password))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        ObjectMapper objectMapper=new  ObjectMapper();

        String senderToken=objectMapper
            .readTree(senderResponse)
            .get("token")
            .asText();

        mockMvc.perform(
            post("/api/wallet/add-money")
                .header("Authorization","Bearer "+senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                    "amount":1000.00
                    }
                """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(1000.00));

        mockMvc.perform(
            post("/api/wallet/transfer")
            .header("Authorization","Bearer "+senderToken)
            .header("Idempotency-Key","tranfer-"+UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "receiverEmail":"%s",
                "amount":300.00
                }
            """.formatted(receiverEmail))
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.balance").value(300.00));


        User sender=userRepository
                    .findByEmail(senderEmail)
                    .orElseThrow();
        
        Wallet senderWallet=walletRepository
                            .findByUser(sender)
                            .orElseThrow();
        
                            assertEquals(new  BigDecimal("700.00"), senderWallet.getBalance());

        User receiver=userRepository
                     .findByEmail(receiverEmail)
                     .orElseThrow();
        
        Wallet receiverWallet=walletRepository
                              .findByUser(receiver)
                              .orElseThrow();
        
        assertEquals(new BigDecimal("300.00"), receiverWallet.getBalance());


        List<Transaction> transactions=transactionRepository
                                 .findBySenderWalletNumberOrReceiverWalletNumber(
                                    senderWallet.getWalletNumber(),
                                     receiverWallet.getWalletNumber());
        assertEquals(1, transactions.size());
        
        Transaction transaction=transactions.get(0);

        assertEquals(TransactionStatus.SUCCESS, transaction.getStatus());
        assertEquals(senderWallet.getWalletNumber(), transaction.getSenderWalletNumber());
        assertEquals(receiverWallet.getWalletNumber(), transaction.getReceiverWalletNumber());
    }
    
    @Test 
    void shouldFailTransferWhenBalanceIsInsufficient() throws Exception{
          
        String senderEmail="sender-"+UUID.randomUUID()+"@gmail.com";
        String receiverEmail="receiver-"+UUID.randomUUID()+"@gmail.com";
        String password="password123";

        mockMvc.perform(
            post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "name":"Sender",
                "email":"%s",
                "password":"%s"
                }
            """.formatted(senderEmail,password))
        )
        .andExpect(status().isOk());

        mockMvc.perform(
            post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "name":"Receiver",
                "email":"%s",
                "password":"%s"
                }
            """.formatted(receiverEmail,password))
        )
        .andExpect(status().isOk());


        String loginResponse=mockMvc.perform(
            post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "email":"%s",
                "password":"%s"
                }
            """.formatted(senderEmail,password))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        ObjectMapper objectMapper=new ObjectMapper();

        String token=objectMapper
               .readTree(loginResponse)
               .get("token")
               .asText();

        mockMvc.perform(
            post("/api/wallet/add-money")
            .header("Authorization","Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "amount":500.00
                 }
            """)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
            post("/api/wallet/transfer")
            .header("Authorization","Bearer " + token)
            .header("Idempotency-Key","insufficient-"+UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "receiverEmail":"%s",
                "amount":600.00
                }
            """.formatted(receiverEmail))
        )
        .andExpect(status().isBadRequest());

        User sender=userRepository
             .findByEmail(senderEmail)
             .orElseThrow();

        Wallet senderWallet=walletRepository
               .findByUser(sender)
               .orElseThrow();

        assertEquals(new  BigDecimal("500.00"), senderWallet.getBalance());

    }
    
    @Test 
    void  shouldFailTransferToSelf() throws Exception{
       
        String email="self-"+UUID.randomUUID()+"@gmail.com";
        String password="password123";

        mockMvc.perform(
            post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "name":"Self User",
                "email":"%s",
                "password":"%s"
                }
            """.formatted(email,password))
        )
        .andExpect(status().isOk());

        String loginResponse=mockMvc.perform(
            post("/api/auth/login")
               .contentType(MediaType.APPLICATION_JSON)
               .content("""
                {
                "email":"%s",
                "password":"%s"
                }
               """.formatted(email,password))
        )
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();

        ObjectMapper objectMapper=new ObjectMapper();

        String token =objectMapper
              .readTree(loginResponse)
              .get("token")
              .asText();

        mockMvc.perform(
            post("/api/wallet/add-money")
            .header("Authorization","Bearer "+ token)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "amount":500.00
                }
            """)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
            post("/api/wallet/transfer")
            .header("Authorization","Bearer "+ token)
            .header("Idempotency-Key","self-"+ UUID.randomUUID())
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                "receiverEmail":"%s",
                "amount":100.00
                }
            """.formatted(email))
        )
        .andExpect(status().isBadRequest());

        User user=userRepository
                .findByEmail(email)
                .orElseThrow();


        Wallet wallet=walletRepository
                   .findByUser(user)
                   .orElseThrow();

        assertEquals(new BigDecimal("500.00"), wallet.getBalance());

    }
}
