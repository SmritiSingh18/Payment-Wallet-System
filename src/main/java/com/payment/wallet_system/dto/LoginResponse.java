package com.payment.wallet_system.dto;

import com.payment.wallet_system.entity.Role;

import lombok.Data;

@Data 
public class LoginResponse {
    private Long id;
    private  String name;
    private String email;
    private  Role role;
    private String token;
}
