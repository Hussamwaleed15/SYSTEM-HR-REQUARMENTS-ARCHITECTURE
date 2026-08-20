package com.services.auth.service;

import com.services.auth.config.LdapProperties;
import com.services.auth.dto.*;
import com.services.auth.enums.Role;
import com.services.auth.enums.UserStatus;
import com.services.auth.model.User;
import com.services.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationClient notificationClient;
    private final LdapProperties ldapProperties;
    private final LdapAuthService ldapAuthService;

    @Value("${auth.reset-token.expiration-minutes:15}")
    private int resetTokenExpirationMinutes;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        if (request.getRole() == null) {
            throw new RuntimeException("Role is required");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmployeeId(request.getEmployeeId());
        user.setDepartment(request.getDepartment());
        user.setRole(request.getRole());
        user.setStatus(UserStatus.ACTIVE);
        user.setIsDeleted(false);

        User savedUser = userRepository.save(user);

        return generateAuthResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. If LDAP is enabled, attempt LDAP authentication first
        if (ldapProperties != null && ldapProperties.isEnabled()) {
            Optional<LdapUserData> ldapUserOpt = ldapAuthService.authenticate(request.getUsername(), request.getPassword());
            if (ldapUserOpt.isPresent()) {
                User syncedUser = syncLdapUser(ldapUserOpt.get(), request.getPassword());
                return generateAuthResponse(syncedUser);
            }
            log.info("LDAP authentication was not successful for user '{}', falling back to database authentication", request.getUsername());
        }

        // 2. Standard DB authentication
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (user.getStatus() != null && user.getStatus() != UserStatus.ACTIVE) {
            throw new RuntimeException("User account is not active");
        }

        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            throw new RuntimeException("User account has been deleted");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }

        return generateAuthResponse(user);
    }

    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        // Generate 6-digit OTP code
        int otpNum = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(otpNum);

        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));
        userRepository.save(user);

        // Dispatch notification via notification service
        notificationClient.sendPasswordResetEmail(user.getEmail(), otp, resetTokenExpirationMinutes);

        return new ApiResponse(true, "Password reset verification code has been sent to your email", otp);
    }

    public ApiResponse verifyResetCode(VerifyResetCodeRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        validateResetCode(user, request.getCode());

        return new ApiResponse(true, "Verification code is valid");
    }

    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        validateResetCode(user, request.getCode());

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return new ApiResponse(true, "Password has been reset successfully");
    }

    private void validateResetCode(User user, String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new RuntimeException("Reset code is required and cannot be blank");
        }
        if (user.getResetToken() == null || !user.getResetToken().equals(code.trim())) {
            throw new RuntimeException("Invalid reset code");
        }

        if (user.getResetTokenExpiry() == null || LocalDateTime.now().isAfter(user.getResetTokenExpiry())) {
            throw new RuntimeException("Reset code has expired. Please request a new one.");
        }
    }

    private User syncLdapUser(LdapUserData ldapData, String rawPassword) {
        Optional<User> existingUserOpt = userRepository.findByUsername(ldapData.getUsername());
        User user;
        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            user.setLdapDn(ldapData.getLdapDn());
            if (ldapData.getEmail() != null && !ldapData.getEmail().isBlank()) {
                user.setEmail(ldapData.getEmail());
            }
            if (ldapData.getFirstName() != null && !ldapData.getFirstName().isBlank()) {
                user.setFirstName(ldapData.getFirstName());
            }
            if (ldapData.getLastName() != null && !ldapData.getLastName().isBlank()) {
                user.setLastName(ldapData.getLastName());
            }
            if (ldapData.getDepartment() != null && !ldapData.getDepartment().isBlank()) {
                user.setDepartment(ldapData.getDepartment());
            }
            user.setPassword(passwordEncoder.encode(rawPassword));
        } else {
            user = new User();
            user.setUsername(ldapData.getUsername());
            user.setEmail(ldapData.getEmail());
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFirstName(ldapData.getFirstName());
            user.setLastName(ldapData.getLastName());
            user.setDepartment(ldapData.getDepartment());
            user.setRole(Role.EMPLOYEE);
            user.setStatus(UserStatus.ACTIVE);
            user.setLdapDn(ldapData.getLdapDn());
            user.setIsDeleted(false);
        }
        return userRepository.save(user);
    }

    private AuthResponse generateAuthResponse(User user) {
        // Embed role as a claim in the JWT so other services can extract it without DB calls
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("roles", List.of("ROLE_" + user.getRole().name()));

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
        String token = jwtService.generateToken(extraClaims, userDetails);
        return new AuthResponse(token, user.getUsername(), user.getEmail(), user.getRole());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}