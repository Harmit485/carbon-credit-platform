package com.carboncredit.controller;

import com.carboncredit.model.CarbonCredit;
import com.carboncredit.repository.CarbonCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/credits")
public class CarbonCreditController {
    @Autowired
    private CarbonCreditRepository carbonCreditRepository;

    @GetMapping
    public List<CarbonCredit> getAllCredits() {
        return carbonCreditRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarbonCredit> getCreditById(@PathVariable String id) {
        return carbonCreditRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/owner/{ownerId}")
    public List<CarbonCredit> getCreditsByOwner(@PathVariable String ownerId) {
        return carbonCreditRepository.findByOwnerId(ownerId);
    }

    @GetMapping("/project/{projectId}")
    public List<CarbonCredit> getCreditsByProject(@PathVariable String projectId) {
        return carbonCreditRepository.findByProjectId(projectId);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<CarbonCredit> getMyCredits() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();
        return carbonCreditRepository.findByOwnerId(userDetails.getId());
    }

    @GetMapping("/status/{status}")
    public List<CarbonCredit> getCreditsByStatus(@PathVariable CarbonCredit.CreditStatus status) {
        return carbonCreditRepository.findByStatus(status);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public CarbonCredit createCredit(@RequestBody CarbonCredit credit) {
        return carbonCreditRepository.save(credit);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CarbonCredit> updateCredit(@PathVariable String id, @RequestBody CarbonCredit creditDetails) {
        return carbonCreditRepository.findById(id)
                .map(credit -> {
                    credit.setQuantity(creditDetails.getQuantity());
                    credit.setVintageYear(creditDetails.getVintageYear());
                    credit.setPricePerUnit(creditDetails.getPricePerUnit());
                    credit.setStatus(creditDetails.getStatus());
                    return ResponseEntity.ok(carbonCreditRepository.save(credit));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> verifyCredit(@PathVariable String id,
            @RequestBody java.util.Map<String, String> requestBody) {
        String status = requestBody.get("status");
        return carbonCreditRepository.findById(id)
                .map(credit -> {
                    try {
                        CarbonCredit.CreditStatus creditStatus = CarbonCredit.CreditStatus.valueOf(status);
                        credit.setStatus(creditStatus);
                        if (creditStatus == CarbonCredit.CreditStatus.VERIFIED) {
                            credit.setVerifiedAt(java.time.LocalDateTime.now());
                        }
                        return ResponseEntity.ok(carbonCreditRepository.save(credit));
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest().body("Invalid status: " + status);
                    }
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Search and filter endpoints
    @GetMapping("/search")
    public List<CarbonCredit> searchCredits(
            @RequestParam(required = false) String projectId,
            @RequestParam(required = false) Integer vintageYear,
            @RequestParam(required = false) CarbonCredit.CreditStatus status,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {

        List<CarbonCredit> allCredits = carbonCreditRepository.findAll();

        return allCredits.stream()
                .filter(credit -> projectId == null || credit.getProjectId().equals(projectId))
                .filter(credit -> vintageYear == null || credit.getVintageYear() == vintageYear)
                .filter(credit -> status == null || credit.getStatus() == status)
                .filter(credit -> minPrice == null || credit.getPricePerUnit() >= minPrice)
                .filter(credit -> maxPrice == null || credit.getPricePerUnit() <= maxPrice)
                .collect(Collectors.toList());
    }

    @Autowired
    private com.carboncredit.service.CreditService creditService;

    @PostMapping("/generate")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> generateCredits(@RequestBody java.util.Map<String, Object> request) {
        String projectId = (String) request.get("projectId");
        Double amount = Double.valueOf(request.get("amount").toString());

        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();

        try {
            creditService.generateCredits(projectId, amount, userDetails.getId());
            return ResponseEntity.ok("Credits generated successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/consume")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<?> consumeCredits(@RequestBody java.util.Map<String, Object> request) {
        String projectId = (String) request.get("projectId");
        Double amount = Double.valueOf(request.get("amount").toString());

        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        com.carboncredit.security.UserDetailsImpl userDetails = (com.carboncredit.security.UserDetailsImpl) authentication
                .getPrincipal();

        try {
            creditService.consumeCredits(projectId, amount, userDetails.getId());
            return ResponseEntity.ok("Credits consumed successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}