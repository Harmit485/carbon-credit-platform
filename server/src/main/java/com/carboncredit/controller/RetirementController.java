package com.carboncredit.controller;

import com.carboncredit.model.Retirement;
import com.carboncredit.repository.RetirementRepository;
import com.carboncredit.repository.CarbonCreditRepository;
import com.carboncredit.repository.WalletRepository;
import com.carboncredit.security.UserDetailsImpl;
import com.carboncredit.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/retirements")
public class RetirementController {

    @Autowired
    private RetirementRepository retirementRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> retireCredits(@RequestBody RetirementRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            String userId = userDetails.getId();

            if (request.getQuantity() <= 0) {
                return ResponseEntity.badRequest().body("Quantity must be greater than 0");
            }

            // Deduct credits from unified wallet balance atomically
            try {
                walletService.retireCredits(userId, request.getQuantity());
            } catch (RuntimeException e) {
                return ResponseEntity.badRequest().body("Insufficient credits in wallet");
            }

            // Create retirement record
            Retirement retirement = new Retirement();
            retirement.setUserId(userId);
            // retirement.setCreditId(request.getCreditId()); // Removed
            retirement.setQuantity(request.getQuantity());
            retirement.setBeneficiaryName(request.getBeneficiaryName());
            retirement.setRetirementReason(request.getRetirementReason());
            retirement
                    .setCertificateUrl("https://carbon-credit-platform.com/certificates/" + System.currentTimeMillis());

            Retirement savedRetirement = retirementRepository.save(retirement);

            return ResponseEntity.ok(savedRetirement);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retiring credits: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<Retirement> getUserRetirements() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return retirementRepository.findByUserId(userDetails.getId());
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<Retirement> getAllRetirements() {
        return retirementRepository.findAll();
    }

    // Request DTO
    public static class RetirementRequest {
        // private String creditId; // Removed
        private double quantity;
        private String beneficiaryName;
        private String retirementReason;

        // Getters and setters
        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public String getBeneficiaryName() {
            return beneficiaryName;
        }

        public void setBeneficiaryName(String beneficiaryName) {
            this.beneficiaryName = beneficiaryName;
        }

        public String getRetirementReason() {
            return retirementReason;
        }

        public void setRetirementReason(String retirementReason) {
            this.retirementReason = retirementReason;
        }
    }
}