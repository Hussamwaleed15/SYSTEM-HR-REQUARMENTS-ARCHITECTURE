package com.services.auth.service;

import com.services.auth.config.LdapProperties;
import com.services.auth.dto.*;
import com.services.auth.enums.Role;
import com.services.auth.enums.UserStatus;
import com.services.auth.model.User;
import com.services.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private NotificationClient notificationClient;

    @Mock
    private LdapProperties ldapProperties;

    @Mock
    private LdapAuthService ldapAuthService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "resetTokenExpirationMinutes", 15);

        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("hossam");
        sampleUser.setEmail("hossam@example.com");
        sampleUser.setPassword("encoded_password");
        sampleUser.setFirstName("Hossam");
        sampleUser.setLastName("Ali");
        sampleUser.setRole(Role.HR);
        sampleUser.setStatus(UserStatus.ACTIVE);
        sampleUser.setIsDeleted(false);
    }

    @Nested
    @DisplayName("Forgot & Reset Password Tests")
    class PasswordResetTests {

        @Test
        @DisplayName("forgotPassword: User found -> OTP generated and email dispatched")
        void forgotPassword_Success() {
            when(userRepository.findByEmail("hossam@example.com")).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ForgotPasswordRequest request = new ForgotPasswordRequest("hossam@example.com");
            ApiResponse response = authService.forgotPassword(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("verification code has been sent");

            // Verify User entity was updated with 6-digit OTP and expiry
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getResetToken()).matches("^\\d{6}$");
            assertThat(savedUser.getResetTokenExpiry()).isAfter(LocalDateTime.now());

            // Verify email dispatch
            verify(notificationClient).sendPasswordResetEmail(eq("hossam@example.com"), eq(savedUser.getResetToken()), eq(15));
        }

        @Test
        @DisplayName("forgotPassword: User not found -> Throws RuntimeException")
        void forgotPassword_UserNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            ForgotPasswordRequest request = new ForgotPasswordRequest("unknown@example.com");
            assertThatThrownBy(() -> authService.forgotPassword(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found with email");
        }

        @Test
        @DisplayName("verifyResetCode: Valid OTP and unexpired -> Success")
        void verifyResetCode_Success() {
            sampleUser.setResetToken("123456");
            sampleUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByEmail("hossam@example.com")).thenReturn(Optional.of(sampleUser));

            VerifyResetCodeRequest request = new VerifyResetCodeRequest("hossam@example.com", "123456");
            ApiResponse response = authService.verifyResetCode(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).isEqualTo("Verification code is valid");
        }

        @Test
        @DisplayName("verifyResetCode: Invalid OTP code -> Throws RuntimeException")
        void verifyResetCode_InvalidCode() {
            sampleUser.setResetToken("123456");
            sampleUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByEmail("hossam@example.com")).thenReturn(Optional.of(sampleUser));

            VerifyResetCodeRequest request = new VerifyResetCodeRequest("hossam@example.com", "999999");
            assertThatThrownBy(() -> authService.verifyResetCode(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid reset code");
        }

        @Test
        @DisplayName("verifyResetCode: Expired OTP code -> Throws RuntimeException")
        void verifyResetCode_Expired() {
            sampleUser.setResetToken("123456");
            sampleUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(5));
            when(userRepository.findByEmail("hossam@example.com")).thenReturn(Optional.of(sampleUser));

            VerifyResetCodeRequest request = new VerifyResetCodeRequest("hossam@example.com", "123456");
            assertThatThrownBy(() -> authService.verifyResetCode(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Reset code has expired");
        }

        @Test
        @DisplayName("resetPassword: Valid OTP and new password -> Password updated and OTP cleared")
        void resetPassword_Success() {
            sampleUser.setResetToken("123456");
            sampleUser.setResetTokenExpiry(LocalDateTime.now().plusMinutes(10));
            when(userRepository.findByEmail("hossam@example.com")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.encode("NewSecret123!")).thenReturn("new_encoded_password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            ResetPasswordRequest request = new ResetPasswordRequest("hossam@example.com", "123456", "NewSecret123!");
            ApiResponse response = authService.resetPassword(request);

            assertThat(response.isSuccess()).isTrue();
            assertThat(response.getMessage()).contains("Password has been reset successfully");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User updatedUser = userCaptor.getValue();
            assertThat(updatedUser.getPassword()).isEqualTo("new_encoded_password");
            assertThat(updatedUser.getResetToken()).isNull();
            assertThat(updatedUser.getResetTokenExpiry()).isNull();
        }
    }

    @Nested
    @DisplayName("Login & Hybrid LDAP Authentication Tests")
    class LoginTests {

        @Test
        @DisplayName("login: Standard DB login with valid credentials -> Returns AuthResponse with JWT")
        void login_StandardDbSuccess() {
            when(ldapProperties.isEnabled()).thenReturn(false);
            when(userRepository.findByUsername("hossam")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn("mocked_jwt_token");

            LoginRequest request = new LoginRequest("hossam", "password123");
            AuthResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("mocked_jwt_token");
            assertThat(response.getUsername()).isEqualTo("hossam");
            assertThat(response.getEmail()).isEqualTo("hossam@example.com");
            assertThat(response.getRole()).isEqualTo(Role.HR);
        }

        @Test
        @DisplayName("login: LDAP Enabled -> LDAP Auth succeeds -> Auto-sync user and return JWT")
        void login_LdapSuccess() {
            when(ldapProperties.isEnabled()).thenReturn(true);
            LdapUserData ldapUserData = LdapUserData.builder()
                    .username("ldapuser")
                    .email("ldapuser@company.com")
                    .firstName("LDAP")
                    .lastName("User")
                    .department("Engineering")
                    .ldapDn("uid=ldapuser,ou=users,dc=example,dc=com")
                    .build();

            when(ldapAuthService.authenticate("ldapuser", "ldap_secret")).thenReturn(Optional.of(ldapUserData));
            when(userRepository.findByUsername("ldapuser")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("ldap_secret")).thenReturn("encoded_ldap_secret");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                return u;
            });
            when(jwtService.generateToken(any())).thenReturn("ldap_jwt_token");

            LoginRequest request = new LoginRequest("ldapuser", "ldap_secret");
            AuthResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("ldap_jwt_token");
            assertThat(response.getUsername()).isEqualTo("ldapuser");
            assertThat(response.getEmail()).isEqualTo("ldapuser@company.com");
            assertThat(response.getRole()).isEqualTo(Role.EMPLOYEE);

            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("login: LDAP Enabled -> LDAP Auth fails -> Falls back to DB auth successfully")
        void login_LdapFallbackToDbSuccess() {
            when(ldapProperties.isEnabled()).thenReturn(true);
            when(ldapAuthService.authenticate("hossam", "password123")).thenReturn(Optional.empty());
            when(userRepository.findByUsername("hossam")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);
            when(jwtService.generateToken(any())).thenReturn("db_fallback_jwt_token");

            LoginRequest request = new LoginRequest("hossam", "password123");
            AuthResponse response = authService.login(request);

            assertThat(response.getToken()).isEqualTo("db_fallback_jwt_token");
            assertThat(response.getUsername()).isEqualTo("hossam");
        }

        @Test
        @DisplayName("login: Invalid DB password -> Throws RuntimeException")
        void login_InvalidPassword() {
            when(ldapProperties.isEnabled()).thenReturn(false);
            when(userRepository.findByUsername("hossam")).thenReturn(Optional.of(sampleUser));
            when(passwordEncoder.matches("wrong_pass", "encoded_password")).thenReturn(false);

            LoginRequest request = new LoginRequest("hossam", "wrong_pass");
            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid username or password");
        }
    }
}
