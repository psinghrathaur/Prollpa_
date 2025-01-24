package com.prollpa.exception;

import org.springframework.http.HttpStatus;


import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import com.prollpa.exception.*;


import io.jsonwebtoken.ExpiredJwtException;



public class GlobalException {
	@ExceptionHandler(UsernameNotFoundException.class)
	public ResponseEntity<ExceptionApiResponse> handleUserNotFoundException(UsernameNotFoundException exception,WebRequest webRequest){
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ExceptionApiResponse.builder().status(HttpStatus.NOT_FOUND).message(exception.getMessage()).description(webRequest.getDescription(false)).build());		
	}
	@ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ExceptionApiResponse> handleExpiredJwtException(ExpiredJwtException exception, WebRequest webRequest) {
        ExceptionApiResponse response = ExceptionApiResponse.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message("Token has expired. Please login again.")
                .description(webRequest.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

	@ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionApiResponse> handleException(Exception exception, WebRequest webRequest) {
        ExceptionApiResponse response = ExceptionApiResponse.builder()
                .status(HttpStatus.NOT_FOUND) // Changed to 401 Unauthorized
                .message("not Found") // More specific message
                .description(webRequest.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
	@ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ExceptionApiResponse> handleResourceNotFoundException(ResourceNotFoundException exception, WebRequest webRequest) {
        ExceptionApiResponse response = ExceptionApiResponse.builder()
                .status(HttpStatus.NOT_FOUND) // Changed to 401 Unauthorized
                .message("not Found") // More specific message
                .description(webRequest.getDescription(false))
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
	@ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage());
    }
	
}
