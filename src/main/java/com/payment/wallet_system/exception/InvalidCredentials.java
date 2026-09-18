package com.payment.wallet_system.exception;

public class InvalidCredentials extends RuntimeException {
    public  InvalidCredentials(String message){
        super(message);
    }
}
