package com.institutojf.mottainai.service;

import com.institutojf.mottainai.config.StaffProperties;
import com.institutojf.mottainai.dto.request.CustomerLoginRequest;
import com.institutojf.mottainai.model.*;
import com.institutojf.mottainai.repository.CustomerAuthRepository;
import com.institutojf.mottainai.security.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailSendException;
import com.institutojf.mottainai.dto.request.CustomerPasswordRecoveryRequest;
import com.institutojf.mottainai.exception.CustomerRecoveryDeliveryException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerAuthenticationServiceTest {

    @Mock
    private CustomerAuthRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenHashService tokenHashService;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MailProperties mailProperties;

    @Mock
    private StaffProperties staffProperties;

    @InjectMocks
    private CustomerAuthenticationService service;

    @Test
    @DisplayName("Should log in active customer and clears failed attempts")
    void logsInActiveCustomerAndClearsFailedAttempts() {
        CustomerAuth auth = auth();
        auth.setFailedAttempts(2);
        when(repository.findByLoginEmailForUpdate("ana@example.com")).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("Strong1!", "hash")).thenReturn(true);
        when(jwtService.generateAccessToken(any(), any())).thenReturn("token");
        when(jwtProperties.expirationMinutes()).thenReturn(60L);
        var response = service.login(new CustomerLoginRequest("ana@example.com", "Strong1!"));
        assertEquals("token", response.accessToken());
        assertEquals(0, auth.getFailedAttempts());
        assertNull(auth.getLockedUntil());
        verify(repository).save(auth);
    }

    @Test
    @DisplayName("Should lock customer for thirty minutes after fifth failure")
    void locksCustomerForThirtyMinutesAfterFifthFailure() {
        CustomerAuth auth = auth();
        auth.setFailedAttempts(4);
        when(repository.findByLoginEmailForUpdate("ana@example.com")).thenReturn(Optional.of(auth));
        when(passwordEncoder.matches("wrong", "hash")).thenReturn(false);
        assertThrows(BadCredentialsException.class,
                () -> service.login(new CustomerLoginRequest("ana@example.com", "wrong")));
        assertEquals(5, auth.getFailedAttempts());
        assertNotNull(auth.getLockedUntil());
        verify(repository).save(auth);
    }

    @Test
    @DisplayName("Should invalidate recovery token and reports mail failure")
    void invalidatesRecoveryTokenAndReportsMailFailure() {
        CustomerAuth auth = auth();
        when(repository.findByLoginEmailForUpdate("ana@example.com")).thenReturn(Optional.of(auth));
        when(tokenHashService.newToken()).thenReturn("raw-token");
        when(tokenHashService.hash("raw-token")).thenReturn("token-hash");
        when(staffProperties.passwordResetUrl()).thenReturn("https://app/reset");
        doThrow(new MailSendException("smtp unavailable")).when(mailSender)
            .send(any(org.springframework.mail.SimpleMailMessage.class));
        assertThrows(CustomerRecoveryDeliveryException.class,
                () -> service.requestPasswordReset(new CustomerPasswordRecoveryRequest("ana@example.com")));
        assertNull(auth.getRecoveryTokenHash());
        assertNull(auth.getRecoveryExpiresAt());
        verify(repository).saveAndFlush(auth);
    }

    private CustomerAuth auth() {
        Customer customer = new Customer();
        customer.setId(1);
        customer.setActive(true);
        CustomerAuth auth = new CustomerAuth();
        auth.setCustomer(customer);
        auth.setLoginEmail("ana@example.com");
        auth.setPasswordHash("hash");
        auth.setFailedAttempts(0);
        auth.setActive(true);
        return auth;
    }

}
