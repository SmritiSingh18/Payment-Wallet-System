package com.payment.wallet_system.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.payment.wallet_system.entity.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service 
public class JwtService {
    
    @Value ("${jwt.secret}")
    private String secret;

    public SecretKey getSigningKey(){ // converts plain  string key into cryptographic key for signing
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(User user){

    return Jwts.builder()
        .subject(user.getEmail())
        .claim("role", user.getRole().name())
        .issuedAt(new Date())
        .expiration(new  Date(System.currentTimeMillis()+1000*60*60))
        .signWith(getSigningKey())
        .compact();


    }
     public String extractEmail(String token){
        return Jwts.parser()
                 .verifyWith(getSigningKey())
                 .build()
                 .parseSignedClaims(token)
                 .getPayload()
                 .getSubject();

     }
     public  boolean isTokenValid(String token ,User user){
        String email=extractEmail(token);
        return email.equals(user.getEmail());
     }

   
}
