package com.payment.wallet_system.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payment.wallet_system.dto.AddMoneyRequest;
import com.payment.wallet_system.dto.WalletResponse;
import com.payment.wallet_system.entity.AuditEventType;
import com.payment.wallet_system.entity.User;
import com.payment.wallet_system.entity.Wallet;
import com.payment.wallet_system.entity.WalletStatus;
import com.payment.wallet_system.respository.AuditLogRespository;
import com.payment.wallet_system.respository.IdempotencyRepository;
import com.payment.wallet_system.respository.TransactionRepository;
import com.payment.wallet_system.respository.UserRepository;
import com.payment.wallet_system.respository.WalletRepository;

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
    
}
