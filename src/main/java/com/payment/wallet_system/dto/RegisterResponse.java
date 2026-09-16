package com.payment.wallet_system.dto;

import java.time.LocalDateTime;

import com.payment.wallet_system.entity.Role;

import lombok.Data;


@Data 
public class RegisterResponse {
    private  Long id;
    private  String name;
    private  String email;
    private  Role role;
    private  LocalDateTime createdAt;
}
