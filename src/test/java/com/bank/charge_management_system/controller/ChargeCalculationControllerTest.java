package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.ChargeCalculationResult;
import com.bank.charge_management_system.dto.TransactionHistoryDto;
import com.bank.charge_management_system.dto.TransactionRequest;
import com.bank.charge_management_system.service.ChargeCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ChargeCalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChargeCalculationService chargeCalculationService;

    private TransactionRequest testRequest;
    private ChargeCalculationResult testResult;

    @BeforeEach
    void setUp() {
        testRequest = new TransactionRequest();
        testRequest.setTransactionId("TXN001");
        testRequest.setCustomerCode("CUST001");
        testRequest.setTransactionType("ATM_WITHDRAWAL_PARENT");
        testRequest.setAmount(new BigDecimal("1000"));
        testRequest.setChannel("ATM");

        testResult = new ChargeCalculationResult();
        testResult.setTransactionId("TXN001");
        testResult.setCustomerCode("CUST001");
        testResult.setSuccess(true);
        testResult.setMessage("Charges calculated successfully");
        testResult.setTotalCharges(new BigDecimal("20.00"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCalculateCharges_Success() throws Exception {
        // Given
        when(chargeCalculationService.calculateChargesForTransaction(any(TransactionRequest.class)))
                .thenReturn(testResult);

        // When & Then
        mockMvc.perform(post("/api/charges/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.transactionId").value("TXN001"))
                .andExpect(jsonPath("$.data.customerCode").value("CUST001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCalculateCharges_InvalidRequest() throws Exception {
        // Given - invalid request (missing required fields)
        TransactionRequest invalidRequest = new TransactionRequest();

        // When & Then
        mockMvc.perform(post("/api/charges/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransactionHistory_Success() throws Exception {
        // Given
        TransactionHistoryDto dto1 = new TransactionHistoryDto(
                "TXN001", "ATM_WITHDRAWAL_PARENT", new BigDecimal("1000"),
                LocalDateTime.now(), "ATM", "PROCESSED"
        );
        TransactionHistoryDto dto2 = new TransactionHistoryDto(
                "TXN002", "FUNDS_TRANSFER", new BigDecimal("5000"),
                LocalDateTime.now().minusDays(1), "ONLINE", "PROCESSED"
        );
        List<TransactionHistoryDto> history = Arrays.asList(dto1, dto2);

        when(chargeCalculationService.getTransactionHistory("CUST001"))
                .thenReturn(history);

        // When & Then
        mockMvc.perform(get("/api/charges/history/CUST001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].transactionId").value("TXN001"))
                .andExpect(jsonPath("$.data[1].transactionId").value("TXN002"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransactionHistoryPaged_Success() throws Exception {
        // Given
        TransactionHistoryDto dto = new TransactionHistoryDto(
                "TXN001", "ATM_WITHDRAWAL_PARENT", new BigDecimal("1000"),
                LocalDateTime.now(), "ATM", "PROCESSED"
        );
        Page<TransactionHistoryDto> page = new PageImpl<>(Arrays.asList(dto));

        when(chargeCalculationService.getTransactionHistoryPaged(
                eq("CUST001"), isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(0), eq(10), eq("transactionDate"), eq("desc")))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/charges/history/CUST001/paged")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].transactionId").value("TXN001"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransactionHistoryPaged_WithFilters() throws Exception {
        // Given
        TransactionHistoryDto dto = new TransactionHistoryDto(
                "TXN001", "ATM_WITHDRAWAL_PARENT", new BigDecimal("1000"),
                LocalDateTime.now(), "ATM", "PROCESSED"
        );
        Page<TransactionHistoryDto> page = new PageImpl<>(Arrays.asList(dto));

        when(chargeCalculationService.getTransactionHistoryPaged(
                eq("CUST001"), eq("ATM_WITHDRAWAL_PARENT"), eq("ATM"), eq("PROCESSED"),
                any(), any(), eq(0), eq(20), eq("transactionDate"), eq("asc")))
                .thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/charges/history/CUST001/paged")
                        .param("transactionType", "ATM_WITHDRAWAL_PARENT")
                        .param("channel", "ATM")
                        .param("status", "PROCESSED")
                        .param("page", "0")
                        .param("size", "20")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetTransactionHistory_CustomerNotFound() throws Exception {
        // Given
        when(chargeCalculationService.getTransactionHistory("INVALID"))
                .thenThrow(new IllegalArgumentException("Customer not found"));

        // When & Then
        mockMvc.perform(get("/api/charges/history/INVALID"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void testCalculateCharges_Unauthorized() throws Exception {
        // When & Then - No authentication
        mockMvc.perform(post("/api/charges/calculate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());
    }
}
