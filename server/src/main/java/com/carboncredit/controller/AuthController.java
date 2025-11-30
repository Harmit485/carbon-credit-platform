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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    WalletRepository walletRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Attempting to authenticate user: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            logger.info("JWT token generated successfully for user: {}", loginRequest.getEmail());

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    roles));
        } catch (Exception e) {
            logger.error("Authentication failed for user: {}", loginRequest.getEmail(), e);
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Authentication failed!"));
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User();
        user.setName(signUpRequest.getName());
        user.setEmail(signUpRequest.getEmail());
        user.setPassword(encoder.encode(signUpRequest.getPassword()));
        user.setOrganization(signUpRequest.getOrganization());
        user.setCountry(signUpRequest.getCountry());

        Set<User.Role> roles = new HashSet<>();

        if (signUpRequest.getRoles() == null || signUpRequest.getRoles().isEmpty()) {
            roles.add(User.Role.ROLE_USER);
        } else {
            signUpRequest.getRoles().forEach(role -> {
                switch (role.toLowerCase()) {
                    case "admin":
                        roles.add(User.Role.ROLE_ADMIN);
                        break;
                    default:
                        roles.add(User.Role.ROLE_USER);
                }
            });
        }

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Create wallet for new user
        Wallet wallet = new Wallet();
        wallet.setUserId(savedUser.getId());
        wallet.setBalance(1000.0); // Initial balance for demo
        walletRepository.save(wallet);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/debug/users")
    public ResponseEntity<?> debugUsers() {
        long count = userRepository.count();
        java.util.List<String> emails = userRepository.findAll().stream()
                .map(User::getEmail)
                .collect(Collectors.toList());

        java.util.Map<String, Object> debug = new java.util.HashMap<>();
        debug.put("count", count);
        debug.put("emails", emails);

        return ResponseEntity.ok(debug);
    }
}