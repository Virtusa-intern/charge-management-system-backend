package com.bank.charge_management_system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String error;
    private Integer errorCode;
    
    // Success response constructors
    public ApiResponse(T data) {
        this.success = true;
        this.message = "Operation completed successfully";
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(String message, T data) {
        this.success = true;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    // Error response constructors
    public ApiResponse(String error) {
        this.success = false;
        this.error = error;
        this.message = "Operation failed";
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(String error, Integer errorCode) {
        this.success = false;
        this.error = error;
        this.errorCode = errorCode;
        this.message = "Operation failed";
        this.timestamp = LocalDateTime.now();
    }
    
    // Static factory methods
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data);
    }
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(message, data);
    }
    
    public static <T> ApiResponse<T> error(String error) {
        return new ApiResponse<>(error);
    }
    
    public static <T> ApiResponse<T> error(String error, Integer errorCode) {
        return new ApiResponse<>(error, errorCode);
    }

    public static <T> ApiResponse<T> error(String error, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setError(error);
        response.setMessage("Operation failed");
        response.setData(data); // This allows Map<String, Object>
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}