package com.payment.wallet_system.integration;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.test.web.servlet.MvcResult; 

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

@SpringBootTest 
@AutoConfigureMockMvc 
public class ConcurrencyIntegrationTest {
    

    @Autowired 
    private MockMvc mockMvc;
    
    @Autowired 
    private WalletRepository walletRepository;
    
    @Autowired 
    private UserRepository userRepository;
    
    @Autowired 
    private TransactionRepository transactionRepository;

    @Test 
    void shouldHandleConcurrentTransfers() throws Exception{
        String senderEmail="sender-"+System.currentTimeMillis()+"@test.com";
        String receiverEmail="receiver-"+System.currentTimeMillis()+"@test.com";

        String senderJson="""
                {
                "name":"Concurrent Sender",
                "email":"%s",
                "password":"password123"
                 }
                """.formatted(senderEmail);

        String receiverJson="""
                { 
                 "name":"Concurrent Receiver",
                 "email":"%s",
                 "password":"password123"
                }
                """.formatted(receiverEmail);

        mockMvc.perform(
            post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(senderJson)
        )
        .andExpect(status().isOk());

        mockMvc.perform(
            post("/api/users")
               .contentType(MediaType.APPLICATION_JSON)
               .content(receiverJson)
            
        )
        .andExpect(status().isOk());

        String loginJson="""
                {
                "email":"%s",
                "password":"password123"
                 }
                """.formatted(senderEmail);
                
        String loginResponse=mockMvc.perform(
            post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJson)
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

        String addMoneyJson="""
                {
                   "amount":10000
                 }
                """;
        mockMvc.perform(
            post("/api/wallet/add-money")
            .header("Authorization","Bearer " +token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(addMoneyJson)

        )
        .andDo(print())
        .andExpect(status().isOk());

        int numberOfTransfers=10;
        int workerCount=numberOfTransfers;


        ExecutorService executor=Executors.newFixedThreadPool(workerCount);
        CountDownLatch ready=new CountDownLatch(workerCount);
        CountDownLatch start=new CountDownLatch(1);
        CountDownLatch finish=new CountDownLatch(numberOfTransfers);

        AtomicInteger successfulTransfer=new  AtomicInteger(0);
        AtomicInteger failedTransfer=new AtomicInteger(0);
        AtomicReference<Throwable> firstError=new AtomicReference<>();

        String transferJson="""
                {
                "receiverEmail":"%s",
                "amount":100
                 }
                """.formatted(receiverEmail);

        for(int i=0;i< numberOfTransfers;i++){
            executor.submit(()->{
                ready.countDown();

                try{
                    start.await();

                 MvcResult result=   mockMvc.perform(
                        post("/api/wallet/transfer")
                        .header("Authorization","Bearer "+token)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(transferJson)
                    )
                    .andReturn();
                 
                int statusCode=result.getResponse().getStatus();
                String responseBody=result.getResponse().getContentAsString();


                if(statusCode==200){
                    successfulTransfer.incrementAndGet();
                }else{
                    failedTransfer.incrementAndGet();
    
                    System.out.println("Transfer Failed-HTTP "
                        +"-"
                        +statusCode +" |Body: "+responseBody
                    );
                    firstError.compareAndSet(null, 
                    new AssertionError(
                        "Transfer returned HTTP"
                        +statusCode
                        +":"
                        +responseBody
                    )
                );
            }   
            }
                catch(Exception e){
                   failedTransfer.incrementAndGet();
                   firstError.compareAndSet(null, e);

                }finally{
                    finish.countDown();
                }
            });
        }

        
         try {
            assertTrue(ready.await(30, TimeUnit.SECONDS),
                    "Workers did not become ready in time");
            start.countDown();
            assertTrue(finish.await(120, TimeUnit.SECONDS),
                    "Transfers did not finish in time");
        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        assertTrue(executor.awaitTermination(120, TimeUnit.SECONDS),
                "Executor did not terminate in time");

        assertEquals(0, failedTransfer.get(),
                "No transfer should fail. First error: " + firstError.get());
        assertEquals(numberOfTransfers, successfulTransfer.get(),
                "All transfers should succeed");

        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        User receiver = userRepository.findByEmail(receiverEmail).orElseThrow();

        Wallet senderWallet = walletRepository.findByUser(sender).orElseThrow();
        Wallet receiverWallet = walletRepository.findByUser(receiver).orElseThrow();

        // 10000 added - (10 x 100) transferred = 9000
        assertEquals(0, senderWallet.getBalance().compareTo(new BigDecimal("9000.00")),
                "Sender balance should be ₹9000");

        assertEquals(0, receiverWallet.getBalance().compareTo(new BigDecimal("1000.00")),
                "Receiver balance should be ₹1000");

        assertTrue(senderWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                "Sender balance must never be negative");

        long transactionCount = transactionRepository
                .findBySenderWalletNumberOrReceiverWalletNumber(
                        senderWallet.getWalletNumber(),
                        receiverWallet.getWalletNumber())
                .size();

        assertEquals(numberOfTransfers, transactionCount,
                "There should be exactly " + numberOfTransfers + " transaction records");
    }
    
        @Test
    void shouldPreventConcurrentDoubleSpend() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String senderEmail = "double-spend-sender-" + suffix + "@test.com";
        String receiverEmail = "double-spend-receiver-" + suffix + "@test.com";

        String senderJson = """
                {
                    "name": "Double Spend Sender",
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(senderEmail);

        String receiverJson = """
                {
                    "name": "Double Spend Receiver",
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(receiverEmail);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(senderJson))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(receiverJson))
                .andExpect(status().isOk());

        String loginJson = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(senderEmail);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        String token = mapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(post("/api/wallet/add-money")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount": 400}
                        """))
                .andExpect(status().isOk());

        int workerCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(workerCount);
        CountDownLatch ready = new CountDownLatch(workerCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(workerCount);

        AtomicInteger successes = new AtomicInteger();
        AtomicInteger insufficientBalance = new AtomicInteger();
        AtomicReference<Throwable> firstError = new AtomicReference<>();

        String transferJson = """
                {
                    "receiverEmail": "%s",
                    "amount": 400
                }
                """.formatted(receiverEmail);

        for (int i = 0; i < workerCount; i++) {
            final int requestNumber = i;
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();

                    MvcResult result = mockMvc.perform(post("/api/wallet/transfer")
                            .header("Authorization", "Bearer " + token)
                            .header("Idempotency-Key", UUID.randomUUID().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transferJson))
                            .andReturn();

                    int code = result.getResponse().getStatus();
                    String body = result.getResponse().getContentAsString();

                    if (code == 200) {
                        successes.incrementAndGet();
                    } else if (code == 400 && body.contains("Insufficient balance")) {
                        insufficientBalance.incrementAndGet();
                    } else {
                        firstError.compareAndSet(null,
                                new AssertionError("Request " + requestNumber
                                        + " returned HTTP " + code + ": " + body));
                    }
                } catch (Throwable e) {
                    firstError.compareAndSet(null, e);
                } finally {
                    finish.countDown();
                }
            });
        }

        try {
            assertTrue(ready.await(30, TimeUnit.SECONDS),
                    "Workers did not become ready in time");
            start.countDown();
            assertTrue(finish.await(120, TimeUnit.SECONDS),
                    "Transfers did not finish in time");
        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        assertTrue(executor.awaitTermination(120, TimeUnit.SECONDS),
                "Executor did not terminate in time");

        assertEquals(1, successes.get(), "Exactly one transfer should succeed");
        assertEquals(1, insufficientBalance.get(),
                "Exactly one transfer should fail for insufficient balance");
        assertEquals(null, firstError.get(),
                "Unexpected transfer response: " + firstError.get());

        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        User receiver = userRepository.findByEmail(receiverEmail).orElseThrow();

        Wallet senderWallet = walletRepository.findByUser(sender).orElseThrow();
        Wallet receiverWallet = walletRepository.findByUser(receiver).orElseThrow();

        assertEquals(0, senderWallet.getBalance().compareTo(BigDecimal.ZERO),
                "Sender balance should be zero");

        assertEquals(0, receiverWallet.getBalance().compareTo(new BigDecimal("400.00")),
                "Receiver balance should be ₹400");

        assertTrue(senderWallet.getBalance().compareTo(BigDecimal.ZERO) >= 0,
                "Sender balance must never be negative");

        long transactionCount = transactionRepository
                .findBySenderWalletNumberOrReceiverWalletNumber(
                        senderWallet.getWalletNumber(),
                        receiverWallet.getWalletNumber())
                .size();

        assertEquals(1, transactionCount,
                "Exactly one successful transaction should be recorded");
    }
        @Test
    void shouldNotProcessSamePaymentTwice() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String senderEmail = "idempotency-sender-" + suffix + "@test.com";
        String receiverEmail = "idempotency-receiver-" + suffix + "@test.com";

        // Register sender and receiver
        for (String[] user : new String[][] {
                {"Idempotency Sender", senderEmail},
                {"Idempotency Receiver", receiverEmail}
        }) {
            String userJson = """
                    {
                        "name": "%s",
                        "email": "%s",
                        "password": "password123"
                    }
                    """.formatted(user[0], user[1]);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                    .andExpect(status().isOk());
        }

        // Log in as sender
        String loginJson = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(senderEmail);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("token")
                .asText();

        // Fund sender with ₹500
        mockMvc.perform(post("/api/wallet/add-money")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount": 500}
                        """))
                .andExpect(status().isOk());

        // Prepare ONE payment request and ONE idempotency key
        String transferJson = """
                {
                    "receiverEmail": "%s",
                    "amount": 100
                }
                """.formatted(receiverEmail);

        String idempotencyKey = UUID.randomUUID().toString();

        // First request
        mockMvc.perform(post("/api/wallet/transfer")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferJson))
                .andExpect(status().isOk());

        // Retry the exact same request with the same key
        mockMvc.perform(post("/api/wallet/transfer")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(transferJson))
                .andExpect(status().isOk());

        // Verify final balances
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        User receiver = userRepository.findByEmail(receiverEmail).orElseThrow();

        Wallet senderWallet = walletRepository.findByUser(sender).orElseThrow();
        Wallet receiverWallet = walletRepository.findByUser(receiver).orElseThrow();

        assertEquals(0,
                senderWallet.getBalance().compareTo(new BigDecimal("400.00")),
                "Sender should be charged only once");

        assertEquals(0,
                receiverWallet.getBalance().compareTo(new BigDecimal("100.00")),
                "Receiver should receive money only once");

        // Verify only one transaction exists for this transfer
        long transactionCount = transactionRepository
                .findBySenderWalletNumberOrReceiverWalletNumber(
                        senderWallet.getWalletNumber(),
                        receiverWallet.getWalletNumber())
                .size();

        assertEquals(1, transactionCount,
                "Retry must not create a second transaction");
    }

        @Test
    void shouldRejectSameIdempotencyKeyWithDifferentAmount() throws Exception {
        String suffix = UUID.randomUUID().toString();
        String senderEmail = "idem-conflict-sender-" + suffix + "@test.com";
        String receiverEmail = "idem-conflict-receiver-" + suffix + "@test.com";

        // Register both users
        for (String[] user : new String[][] {
                {"Idempotency Sender", senderEmail},
                {"Idempotency Receiver", receiverEmail}
        }) {
            String userJson = """
                    {
                        "name": "%s",
                        "email": "%s",
                        "password": "password123"
                    }
                    """.formatted(user[0], user[1]);

            mockMvc.perform(post("/api/users")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(userJson))
                    .andExpect(status().isOk());
        }

        // Log in as sender
        String loginJson = """
                {
                    "email": "%s",
                    "password": "password123"
                }
                """.formatted(senderEmail);

        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = new ObjectMapper()
                .readTree(loginResponse)
                .get("token")
                .asText();

        // Fund sender
        mockMvc.perform(post("/api/wallet/add-money")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"amount": 500}
                        """))
                .andExpect(status().isOk());

        String idempotencyKey = UUID.randomUUID().toString();

        // First request: transfer ₹100
        String firstRequest = """
                {
                    "receiverEmail": "%s",
                    "amount": 100
                }
                """.formatted(receiverEmail);

        mockMvc.perform(post("/api/wallet/transfer")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(firstRequest))
                .andExpect(status().isOk());

        // Second request: same key, different amount
        String conflictingRequest = """
                {
                    "receiverEmail": "%s",
                    "amount": 200
                }
                """.formatted(receiverEmail);

        mockMvc.perform(post("/api/wallet/transfer")
                .header("Authorization", "Bearer " + token)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .content(conflictingRequest))
                .andExpect(status().isBadRequest());

        // Verify the original payment was processed only once
        User sender = userRepository.findByEmail(senderEmail).orElseThrow();
        User receiver = userRepository.findByEmail(receiverEmail).orElseThrow();

        Wallet senderWallet = walletRepository.findByUser(sender).orElseThrow();
        Wallet receiverWallet = walletRepository.findByUser(receiver).orElseThrow();

        assertEquals(0,
                senderWallet.getBalance().compareTo(new BigDecimal("400.00")));
        assertEquals(0,
                receiverWallet.getBalance().compareTo(new BigDecimal("100.00")));

        long transactionCount = transactionRepository
                .findBySenderWalletNumberOrReceiverWalletNumber(
                        senderWallet.getWalletNumber(),
                        receiverWallet.getWalletNumber())
                .size();

        assertEquals(1, transactionCount);
    }
}