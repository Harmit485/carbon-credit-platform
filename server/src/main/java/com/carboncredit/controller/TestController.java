package com.carboncredit.controller;

import com.carboncredit.model.User;
import com.carboncredit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping({ "/api/test", "/test" })
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            long count = userRepository.count();
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok("User count: " + count + ", Users: " + users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving users: " + e.getMessage());
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        try {
            boolean exists = userRepository.existsByEmail(email);
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                // Create a safe representation without the password
                return ResponseEntity.ok(new SafeUser(user.getId(), user.getName(), user.getEmail(), user.getRoles()));
            } else {
                return ResponseEntity.badRequest()
                        .body("User not found with email: " + email + ", existsByEmail: " + exists);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving user: " + e.getMessage());
        }
    }

    @GetMapping("/db-status")
    public ResponseEntity<?> getDatabaseStatus() {
        try {
            long count = userRepository.count();
            return ResponseEntity.ok("Database connection OK. Total users: " + count);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Database connection error: " + e.getMessage());
        }
    }

    @PostMapping("/check-password")
    public ResponseEntity<?> checkPassword(@RequestBody PasswordCheckRequest request) {
        try {
            Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                boolean matches = passwordEncoder.matches(request.getPassword(), user.getPassword());
                return ResponseEntity
                        .ok("Password matches: " + matches + ". Stored password hash: " + user.getPassword());
            } else {
                return ResponseEntity.badRequest().body("User not found with email: " + request.getEmail());
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error checking password: " + e.getMessage());
        }
    }

    // Inner class to represent user data without password
    static class SafeUser {
        private String id;
        private String name;
        private String email;
        private Object roles;

        public SafeUser(String id, String name, String email, Object roles) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.roles = roles;
        }

        // Getters
        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public Object getRoles() {
            return roles;
        }
    }

    // Inner class for password check request
    static class PasswordCheckRequest {
        private String email;
        private String password;

        public String getEmail() {
            return email;
        }

        public String getPassword() {
            return password;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}