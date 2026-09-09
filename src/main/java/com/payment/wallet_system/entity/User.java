package com.payment.wallet_system.entity;

import java.time.LocalDateTime;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity 
@Table (name = "users")
@Data 

public class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private long id;

    
    private String name;

    @Column (nullable = false,unique = true)
    private  String email;

    @Column (nullable = false)
    private  String password;

    @Enumerated (EnumType.STRING)
    @Column (nullable = false)
    private  Role role;

    private  LocalDateTime createdAt;
}
