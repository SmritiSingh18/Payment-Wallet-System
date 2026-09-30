package com.payment.wallet_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.TransferMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.IdempotencyRecord;
import com.payment.wallet_system.entity.Transaction;
import com.payment.wallet_system.entity.TransactionStatus;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.entity.WalletStatus;
import com.payment.wallet_system.respository.AuditLogRespository;
import com.payment.wallet_system.respository.IdempotencyRepository;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

import io.jsonwebtoken.security.Jwks.OP;

@ExtendWith (MockitoExtension.class)
public class WalletServiceTest {
    @Mock 
    private  UserRepository userRepository;

    @Mock 
    private  WalletRepository walletRepository;

    @Mock 
    private  TransactionRepository transactionRepository;

    @Mock 
    private  IdempotencyRepository idempotencyRepository;

    @Mock 
    private AuditLogRespository auditLogRespository;

    @Mock 
    private AuditLogService auditLogService;
    
    @InjectMocks 
    private WalletService walletService;
    
    @Test 
    void shouldAddMoneyToWallet(){
       User user=new User();
       user.setId(1L);
       user.setName("Test User");
       user.setEmail("test@gmail.com");

       Wallet wallet=new  Wallet();
       wallet.setWalletNumber("WALLET-001");
       wallet.setBalance(new BigDecimal("100"));
       wallet.setStatus(WalletStatus.ACTIVE);
       wallet.setUser(user);

       AddMoneyRequest request=new AddMoneyRequest();
       request.setAmount(new BigDecimal("50"));
       when(userRepository.findByEmail("test@gmail.com"))
           .thenReturn(Optional.of(user));

        when(walletRepository.findByUser(user))
            .thenReturn(Optional.of(wallet));

        when(walletRepository.save(wallet))
            .thenReturn(wallet);


        WalletResponse response=walletService.addMoney("test@gmail.com", request);

        assertEquals(new BigDecimal("150"), response.getBalance());

        verify(walletRepository).save(wallet);

        verify(auditLogService).log(
            user.getId(), AuditEventType.MONEY_ADDED, "Money added:"+request.getAmount(), null);
    }

    @Test 
    void shouldThrowExceptionWhenUserNotFound(){
        when(userRepository.findByEmail("unknown@gmail.com"))
           .thenReturn(Optional.empty());
        
        RuntimeException exception=assertThrows(RuntimeException.class,
            ()-> walletService.addMoney("unknown@gmail.com", new AddMoneyRequest()));

        assertEquals("User not found",exception.getMessage());
    }
    
    @Test 
    void  shouldThrowExceptionWhenWalletNotFound(){
       User user=new User();
       user.setId(1L);
       user.setEmail("test@gmail.com");

       when(userRepository.findByEmail("test@gmail.com"))
           .thenReturn(Optional.of(user));
        
        when(walletRepository.findByUser(user))
            .thenReturn(Optional.empty());
        
        RuntimeException exception=assertThrows(RuntimeException.class,
             ()-> walletService.addMoney("test@gmail.com", new  AddMoneyRequest()));

        assertEquals("Wallet not found", exception.getMessage());
    }


    @Test 
    void shouldTransferMoneySuccessfully(){
         User sender=new User();
         sender.setId(1L);
         sender.setName("Sender");
         sender.setEmail("sender@gmail.com");

         User receiver=new User();
         receiver.setId(2L);
         receiver.setName("Receiver");
         receiver.setEmail("receiver@gmail.com");

         Wallet senderWallet=new Wallet();
         senderWallet.setWalletNumber("WALLET-001");
         senderWallet.setBalance(new  BigDecimal("1000"));
         senderWallet.setStatus(WalletStatus.ACTIVE);
         senderWallet.setUser(sender);

         Wallet receiverWallet=new Wallet();
         receiverWallet.setWalletNumber("WALLET-002");
         receiverWallet.setBalance(new BigDecimal("200"));
         receiverWallet.setStatus(WalletStatus.ACTIVE);
         receiverWallet.setUser(receiver);

         TransferMoneyRequest request=new TransferMoneyRequest();
         request.setReceiverEmail("receiver@gmail.com");
         request.setAmount(new BigDecimal("400"));

         when(idempotencyRepository.findByIdempotencyKey("payment-001"))
               .thenReturn(Optional.empty());
        
         when(userRepository.findByEmail("sender@gmail.com"))
                .thenReturn(Optional.of(sender));

        when(userRepository.findByEmail("receiver@gmail.com"))
                .thenReturn(Optional.of(receiver));

        when(walletRepository.findWithLockByUser(sender))
                 .thenReturn(Optional.of(senderWallet));

        when(walletRepository.findWithLockByUser(receiver))
                  .thenReturn(Optional.of(receiverWallet));

        when(walletRepository.save(senderWallet))
                  .thenReturn(senderWallet);
        
        when(walletRepository.save(receiverWallet))
                  .thenReturn(receiverWallet);

    Transaction transaction=new  Transaction();
    transaction.setId(1L);
    transaction.setTransactionId("TXN-001");
    transaction.setSenderWalletNumber("WALLET-001");
    transaction.setReceiverWalletNumber("WALLET-002");
    transaction.setAmount(new  BigDecimal("400"));
    transaction.setStatus(TransactionStatus.SUCCESS);
    transaction.setCreatedAt(LocalDateTime.now());
      
    when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(transaction);


        WalletResponse response=walletService.transferMoney("sender@gmail.com", request, "payment-001");

        assertEquals(new BigDecimal("600"),senderWallet.getBalance());
        assertEquals(new  BigDecimal("600"),receiverWallet.getBalance());
        verify(transactionRepository).save(any(Transaction.class));
        verify(idempotencyRepository).save(any(IdempotencyRecord.class));
        verify(auditLogService).log(sender.getId(), AuditEventType.TRANSFER_SUCCESS, "Transfer Success", transaction.getId());
        }
        
        @Test 
        void shouldFailTranferForInsufficientBalance(){
          User sender=new User();
          sender.setId(1L);
          sender.setName("Sender");
          sender.setEmail("sender@gmail.com");

          User receiver=new User();
          receiver.setId(2L);
          receiver.setName("Receiver");
          receiver.setEmail("receoiver@gmail.com");

          Wallet senderWallet=new Wallet();
          senderWallet.setWalletNumber("WALLET-001");
          senderWallet.setBalance(new BigDecimal("100"));
          senderWallet.setStatus(WalletStatus.ACTIVE);
          senderWallet.setUser(sender);

          Wallet receiverWallet=new  Wallet();
          receiverWallet.setWalletNumber("WAllET-002");
          receiverWallet.setBalance(new  BigDecimal("200"));
          receiverWallet.setStatus(WalletStatus.ACTIVE);
          receiverWallet.setUser(receiver);

            TransferMoneyRequest request=new  TransferMoneyRequest();
            request.setReceiverEmail("receiver@gmail.com");
            request.setAmount(new BigDecimal("400"));

            when(idempotencyRepository.findByIdempotencyKey("payment-002"))
                  .thenReturn(Optional.empty());
                  
            when(userRepository.findByEmail("sender@gmail.com"))
                   .thenReturn(Optional.of(sender));

            when(userRepository.findByEmail("receiver@gmail.com"))
                   .thenReturn(Optional.of(receiver));

            when(walletRepository.findWithLockByUser(sender))
                   .thenReturn(Optional.of(senderWallet));

            when(walletRepository.findWithLockByUser(receiver))
                    .thenReturn(Optional.of(receiverWallet));

            RuntimeException exception=assertThrows(RuntimeException.class,
                 ()-> walletService.transferMoney(
                    "sender@gmail.com",
                     request,
                      "payment-002"));

                    assertEquals("Insufficient balance", exception.getMessage());
                    verify(transactionRepository,never())
                          .save(any(Transaction.class));
                    verify(auditLogService).log(
                        sender.getId(),
                         AuditEventType.TRANSFER_FAILED,
                          "Transfer Failed-Insufficient balance",
                           null);
        }
        
        @Test 
        void shouldFailTransferWhenSenderAndReceiverAreSame(){
            User user=new User();
            user.setId(1L);
            user.setName("Test user");
            user.setEmail("test@gmail.com");

            Wallet wallet=new  Wallet();
            wallet.setWalletNumber("WALLET-001");
            wallet.setBalance(new BigDecimal("1000"));
            wallet.setStatus(WalletStatus.ACTIVE);
            wallet.setUser(user);

            TransferMoneyRequest request=new TransferMoneyRequest();
            request.setReceiverEmail("test@gmail.com");
            request.setAmount(new BigDecimal("400"));

            when(idempotencyRepository.findByIdempotencyKey("payment-003"))
                 .thenReturn(Optional.empty());

            when(userRepository.findByEmail("test@gmail.com"))
                 .thenReturn(Optional.of(user));

            when(walletRepository.findWithLockByUser(user))
                  .thenReturn(Optional.of(wallet));
             
            RuntimeException exception=assertThrows(RuntimeException.class,
                ()->walletService.transferMoney("test@gmail.com", request, "payment-003") );

            assertEquals("Cannot transfer money to yourself", exception.getMessage());
            verify(transactionRepository,never()).save(any(Transaction.class));
            assertEquals(new BigDecimal("1000"),wallet.getBalance());
        }
        
        @Test 
        void shouldFailTransferWhenSenderWalletIsBlocked(){
           User sender=new User();
           sender.setId(1L);
           sender.setName("Sender");
           sender.setEmail("sender@gmail.com");
            
           User receiver=new User();
           receiver.setId(2L);
           receiver.setName("Receiver");
           receiver.setEmail("receiver@gmail.com");

           Wallet senderWallet=new Wallet();
           senderWallet.setWalletNumber("WALLET-001");
           senderWallet.setBalance(new BigDecimal("1000"));
           senderWallet.setStatus(WalletStatus.BLOCKED);
           senderWallet.setUser(sender);

           Wallet receiverWallet=new Wallet();
           receiverWallet.setWalletNumber("WALLET-002");
           receiverWallet.setBalance(new BigDecimal("200"));
           receiverWallet.setStatus(WalletStatus.BLOCKED);
           receiverWallet.setUser(receiver);

           TransferMoneyRequest request=new  TransferMoneyRequest();
           request.setReceiverEmail("receiver@gmail.com");
           request.setAmount(new BigDecimal("400"));

             when(idempotencyRepository.findByIdempotencyKey("payment-004"))
                  .thenReturn(Optional.empty());

            when(userRepository.findByEmail("sender@gmail.com"))
                   .thenReturn(Optional.of(sender));
            
            when(userRepository.findByEmail("receiver@gmail.com"))
                    .thenReturn(Optional.of(receiver));

            when(walletRepository.findWithLockByUser(sender))
                   .thenReturn(Optional.of(senderWallet));

            when(walletRepository.findWithLockByUser(receiver))
                  .thenReturn(Optional.of(receiverWallet));
               
            RuntimeException exception=assertThrows(RuntimeException.class,
                 ()->walletService.transferMoney(
                    "sender@gmail.com", request,
                     "payment-004"));

            assertEquals("Wallet is not active", exception.getMessage());
            verify(transactionRepository,never()).save(any(Transaction.class));
        }

        @Test 
        void shouldFailTransferWhenReceiverWalletIsBlocked(){
           User sender=new User();
           sender.setId(1L);
           sender.setName("Sender");
           sender.setEmail("sender@gmail.com");

           User receiver=new User();
           receiver.setId(2L);
           receiver.setName("Receiver");
           receiver.setEmail("receiver@gmail.com");

           Wallet senderWallet=new Wallet();
           senderWallet.setWalletNumber("WALLET=001");
           senderWallet.setBalance(new BigDecimal("1000"));
           senderWallet.setStatus(WalletStatus.ACTIVE);
           senderWallet.setUser(sender);

           Wallet receiverWallet=new Wallet();
           receiverWallet.setWalletNumber("WALLET-002");
           receiverWallet.setBalance(new BigDecimal("200"));
           receiverWallet.setStatus(WalletStatus.BLOCKED);
           receiverWallet.setUser(receiver);

           TransferMoneyRequest request=new  TransferMoneyRequest();
           request.setReceiverEmail("receiver@gmail.com");
           request.setAmount(new BigDecimal("400"));

           when(idempotencyRepository.findByIdempotencyKey("payment-005"))
                .thenReturn(Optional.empty());
           
            when(userRepository.findByEmail("sender@gmail.com"))
                .thenReturn(Optional.of(sender));
            
            when(userRepository.findByEmail("receiver@gmail.com"))
                .thenReturn(Optional.of(receiver));

            when(walletRepository.findWithLockByUser(sender))
                .thenReturn(Optional.of(senderWallet));
            
            when(walletRepository.findWithLockByUser(receiver))
                .thenReturn(Optional.of(receiverWallet));

            RuntimeException exception=assertThrows(
                RuntimeException.class,
            ()-> walletService.transferMoney("sender@gmail.com", request, "payment-005"));

            assertEquals("Wallet is not active", exception.getMessage());
            verify(transactionRepository,never()).save(any(Transaction.class));
        }

    
}
