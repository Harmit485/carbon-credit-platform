package com.carboncredit.service;

import com.carboncredit.model.Project;
import com.carboncredit.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private WalletService walletService;

    @Transactional
    public void generateCredits(String projectId, double amount, String userId) {
        System.out.println("Generating credits. Project: " + projectId + ", Amount: " + amount + ", User: " + userId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Verify owner
        if (!project.getIssuerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Update Wallet - Add credits to user's account
        walletService.createWalletIfNotExists(userId);
        walletService.updateBalance(userId, 0.0, amount);
        System.out.println("Credits generated successfully.");
    }

    @Transactional
    public void consumeCredits(String projectId, double amount, String userId) {
        System.out.println("Consuming credits. Project: " + projectId + ", Amount: " + amount + ", User: " + userId);
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        // Verify project type
        if (!isProducingProject(project.getType())) {
            throw new RuntimeException("Only Producing projects can consume credits");
        }

        // Verify owner
        if (!project.getIssuerId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        // Update Wallet - Simple stock-like credit deduction
        walletService.createWalletIfNotExists(userId);
        // Use negative amount to deduct
        walletService.updateBalance(userId, 0.0, -amount);
        System.out.println("Credits consumed successfully.");
    }

    private boolean isReducingProject(Project.ProjectType type) {
        return type == Project.ProjectType.RENEWABLE_ENERGY ||
                type == Project.ProjectType.SOLAR ||
                type == Project.ProjectType.WIND ||
                type == Project.ProjectType.REFORESTATION ||
                type == Project.ProjectType.GREEN_ENERGY;
    }

    private boolean isProducingProject(Project.ProjectType type) {
        return type == Project.ProjectType.MANUFACTURING ||
                type == Project.ProjectType.METAL ||
                type == Project.ProjectType.BURNING ||
                type == Project.ProjectType.INDUSTRIAL_BURNING ||
                type == Project.ProjectType.OTHER;
    }
}
