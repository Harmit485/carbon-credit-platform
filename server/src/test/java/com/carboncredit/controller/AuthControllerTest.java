package com.carboncredit.controller;

import com.carboncredit.model.User;
import com.carboncredit.model.Wallet;
import com.carboncredit.payload.request.LoginRequest;
import com.carboncredit.payload.request.SignupRequest;
import com.carboncredit.payload.response.JwtResponse;
import com.carboncredit.payload.response.MessageResponse;
import com.carboncredit.repository.UserRepository;
import com.carboncredit.repository.WalletRepository;
import com.carboncredit.security.JwtUtils;
import com.carboncredit.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testRegisterUser_Success() {
        SignupRequest req = new SignupRequest();
        req.setEmail("new@test.com");
        req.setPassword("password");
        req.setName("New User");
        req.setRoles(new HashSet<>(Collections.singletonList("buyer")));

        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(encoder.encode("password")).thenReturn("encoded_password");

        User savedUser = new User();
        savedUser.setId("user1");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<?> response = authController.registerUser(req);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);

        // Verify wallet creation
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    public void testAuthenticateUser_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@test.com");
        req.setPassword("password");

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "user@test.com", "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_BUYER")));

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("jwt-token");

        ResponseEntity<?> response = authController.authenticateUser(req);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("jwt-token", jwtResponse.getToken());
        assertEquals("user1", jwtResponse.getId());
    }
}
