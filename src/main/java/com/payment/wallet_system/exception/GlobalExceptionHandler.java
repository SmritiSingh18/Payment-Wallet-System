package com.payment.wallet_system.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice 
public class GlobalExceptionHandler {

    @ExceptionHandler (DuplicateResourceException.class)
    public  ResponseEntity<ErrorResponse>  handleDuplicateResourceException(DuplicateResourceException e){
        ErrorResponse response=new ErrorResponse();
        response.setStatus(409);
        response.setMessage(e.getMessage());
        response.setTimeStamp(LocalDateTime.now());

        return  ResponseEntity.status(409).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
     public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e){
        ErrorResponse response=new ErrorResponse();
        response.setStatus(400);
        response.setMessage(
            e.getBindingResult()
            .getFieldErrors()
            .get(0)
            .getDefaultMessage()
            
        );
        response.setTimeStamp(LocalDateTime.now());

        return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(response);
     }
     @ExceptionHandler (InvalidCredentials.class)
     public  ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentials e){
        ErrorResponse response=new ErrorResponse();
        response.setStatus(401);
        response.setMessage(e.getMessage());
        response.setTimeStamp(LocalDateTime.now());

        return  ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(response);
     } 
    
}
