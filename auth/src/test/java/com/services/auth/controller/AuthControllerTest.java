package com.services.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.services.auth.config.JwtAuthenticationFilter;
import com.services.auth.config.SecurityConfig;
import com.services.auth.dto.*;
import com.services.auth.enums.Role;
import com.services.auth.service.AuthService;
import com.services.auth.service.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtService jwtService;

    @Test
    @DisplayName("POST /api/auth/login -> Returns AuthResponse")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("hossam", "password123");
        AuthResponse response = new AuthResponse("jwt_token_example", "hossam", "hossam@example.com", Role.HR);

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token_example"))
                .andExpect(jsonPath("$.username").value("hossam"))
                .andExpect(jsonPath("$.role").value("HR"));
    }

    @Test
    @DisplayName("POST /api/auth/forgot-password -> Returns ApiResponse")
    void forgotPassword_Success() throws Exception {
        ForgotPasswordRequest request = new ForgotPasswordRequest("user@example.com");
        ApiResponse response = new ApiResponse(true, "Password reset verification code has been sent to your email");

        when(authService.forgotPassword(any(ForgotPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message", containsString("verification code has been sent")));
    }

    @Test
    @DisplayName("POST /api/auth/verify-reset-code -> Returns ApiResponse")
    void verifyResetCode_Success() throws Exception {
        VerifyResetCodeRequest request = new VerifyResetCodeRequest("user@example.com", "123456");
        ApiResponse response = new ApiResponse(true, "Verification code is valid");

        when(authService.verifyResetCode(any(VerifyResetCodeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/verify-reset-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Verification code is valid"));
    }

    @Test
    @DisplayName("POST /api/auth/register -> Valid request returns AuthResponse")
    void register_Success() throws Exception {
        RegisterRequest request = new RegisterRequest("hr_user", "hr@example.com", "Password123!", "Hossam", "Ali", "EMP01", "HR", Role.HR);
        AuthResponse response = new AuthResponse("jwt_token_example", "hr_user", "hr@example.com", Role.HR);

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt_token_example"))
                .andExpect(jsonPath("$.username").value("hr_user"))
                .andExpect(jsonPath("$.role").value("HR"));
    }

    @Test
    @DisplayName("POST /api/auth/register -> Invalid email format returns 400 Bad Request with validation errors")
    void register_InvalidEmail_ReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("hr_user", "invalid-email", "pass", "", "", null, null, null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @DisplayName("POST /api/auth/reset-password -> Returns ApiResponse")
    void resetPassword_Success() throws Exception {
        ResetPasswordRequest request = new ResetPasswordRequest("user@example.com", "123456", "NewSecretPass123!");
        ApiResponse response = new ApiResponse(true, "Password has been reset successfully");

        when(authService.resetPassword(any(ResetPasswordRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password has been reset successfully"));
    }
}
