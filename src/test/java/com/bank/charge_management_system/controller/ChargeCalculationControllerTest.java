package com.bank.charge_management_system.controller;

import com.bank.charge_management_system.dto.ChargeCalculationResult;
import com.bank.charge_management_system.dto.TransactionRequest;
import com.bank.charge_management_system.repository.ChargeCalculationRepository;
import com.bank.charge_management_system.repository.TransactionRepository;
import com.bank.charge_management_system.service.ChargeCalculationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChargeCalculationController.class)
class ChargeCalculationControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private ChargeCalculationService chargeCalculationService;

        @MockBean
        private TransactionRepository transactionRepository;

        @MockBean
        private ChargeCalculationRepository chargeCalculationRepository;

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
                testRequest.setCurrencyCode("INR");

                testResult = new ChargeCalculationResult();
                testResult.setTransactionId("TXN001");
                testResult.setCustomerCode("CUST001");
                testResult.setSuccess(true);
                testResult.setMessage("Charges calculated successfully");
                testResult.setTotalCharges(new BigDecimal("20.00"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = { "USER", "ADMIN" })
        void testCalculateCharges_Success() throws Exception {
                // Given
                when(chargeCalculationService.calculateChargesForTransaction(any(TransactionRequest.class)))
                                .thenReturn(testResult);

                // When & Then
                mockMvc.perform(post("/api/charges/calculate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testRequest)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").value(true))
                                .andExpect(jsonPath("$.data.transactionId").value("TXN001"))
                                .andExpect(jsonPath("$.data.customerCode").value("CUST001"));
        }

        @Test
        @WithMockUser(username = "testuser", roles = { "USER" })
        void testCalculateCharges_InvalidRequest() throws Exception {
                // Given - invalid request (missing required fields)
                TransactionRequest invalidRequest = new TransactionRequest();

                // When & Then
                mockMvc.perform(post("/api/charges/calculate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest)))
                                .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(username = "testuser", roles = { "USER" })
        void testGetChargeStatistics_Success() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/charges/statistics")
                                .with(csrf()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.success").exists());
        }

        @Test
        void testCalculateCharges_Unauthorized() throws Exception {
                // When & Then - No authentication
                mockMvc.perform(post("/api/charges/calculate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testRequest)))
                                .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "testuser", roles = { "USER" })
        void testCalculateCharges_ServiceException() throws Exception {
                // Given
                when(chargeCalculationService.calculateChargesForTransaction(any(TransactionRequest.class)))
                                .thenThrow(new RuntimeException("Service error"));

                // When & Then
                mockMvc.perform(post("/api/charges/calculate")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(testRequest)))
                                .andExpect(status().isInternalServerError());
        }
}
