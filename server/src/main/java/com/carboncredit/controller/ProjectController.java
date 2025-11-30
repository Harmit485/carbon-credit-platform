package com.carboncredit.controller;

import com.carboncredit.model.Project;
import com.carboncredit.repository.ProjectRepository;
import com.carboncredit.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private com.carboncredit.repository.UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<Project> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        projects.forEach(this::populateOwner);
        return projects;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Project> getProjectById(@PathVariable String id) {
        return projectRepository.findById(id)
                .map(project -> {
                    populateOwner(project);
                    return ResponseEntity.ok(project);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public Project createProject(@RequestBody Project project) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Set the issuer ID to the current user's ID
        project.setIssuerId(userDetails.getId());

        // Default status to PENDING for new projects
        project.setStatus(Project.ProjectStatus.PENDING);

        Project savedProject = projectRepository.save(project);
        populateOwner(savedProject);
        return savedProject;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Project> updateProject(@PathVariable String id, @RequestBody Project projectDetails) {
        return projectRepository.findById(id)
                .map(project -> {
                    project.setName(projectDetails.getName());
                    project.setDescription(projectDetails.getDescription());
                    project.setLocation(projectDetails.getLocation());
                    project.setType(projectDetails.getType());
                    project.setTotalCarbonCredits(projectDetails.getTotalCarbonCredits());
                    project.setAvailableCarbonCredits(projectDetails.getAvailableCarbonCredits());
                    project.setDocumentationUrl(projectDetails.getDocumentationUrl());
                    Project updatedProject = projectRepository.save(project);
                    populateOwner(updatedProject);
                    return ResponseEntity.ok(updatedProject);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Autowired
    private com.carboncredit.service.CreditService creditService;

    @PutMapping("/{id}/verify")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Project> verifyProject(@PathVariable String id,
            @RequestBody java.util.Map<String, String> statusUpdate) {
        return projectRepository.findById(id)
                .map(project -> {
                    String status = statusUpdate.get("status");
                    if (status != null) {
                        try {
                            System.out.println("Verifying project: " + id + " to status: " + status);
                            Project.ProjectStatus newStatus = Project.ProjectStatus.valueOf(status);

                            // Handle credits when status changes to VERIFIED
                            if (newStatus == Project.ProjectStatus.VERIFIED
                                    && project.getStatus() != Project.ProjectStatus.VERIFIED) {

                                if (!project.isCreditsGenerated()) {
                                    System.out.println(
                                            "Project " + id + " verified. Crediting user with carbon credits...");

                                    // Generate credits for ALL approved projects
                                    // Admin approval means the user is authorized to emit/offset this amount
                                    creditService.generateCredits(project.getId(), project.getTotalCarbonCredits(),
                                            project.getIssuerId());

                                    project.setCreditsGenerated(true);
                                } else {
                                    System.out.println(
                                            "Project " + id + " verified, but credits were already generated.");
                                }
                            }

                            project.setStatus(newStatus);
                        } catch (IllegalArgumentException e) {
                            System.err.println("Invalid status: " + status);
                            return ResponseEntity.badRequest().<Project>build();
                        }
                    }
                    Project savedProject = projectRepository.save(project);
                    populateOwner(savedProject);
                    return ResponseEntity.ok(savedProject);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Helper method to check if project type is carbon-reducing
    private boolean isReducingProject(Project.ProjectType type) {
        return type == Project.ProjectType.RENEWABLE_ENERGY ||
                type == Project.ProjectType.SOLAR ||
                type == Project.ProjectType.WIND ||
                type == Project.ProjectType.REFORESTATION ||
                type == Project.ProjectType.GREEN_ENERGY;
    }

    // Helper method to check if project type is carbon-producing
    private boolean isProducingProject(Project.ProjectType type) {
        return type == Project.ProjectType.MANUFACTURING ||
                type == Project.ProjectType.METAL ||
                type == Project.ProjectType.BURNING ||
                type == Project.ProjectType.INDUSTRIAL_BURNING ||
                type == Project.ProjectType.OTHER;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> deleteProject(@PathVariable String id) {
        return projectRepository.findById(id)
                .map(project -> {
                    projectRepository.delete(project);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<Project> getMyProjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        List<Project> projects = projectRepository.findByIssuerId(userDetails.getId());
        projects.forEach(this::populateOwner);
        return projects;
    }

    // Search and filter endpoints
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    public List<Project> searchProjects(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Project.ProjectType type,
            @RequestParam(required = false) Project.ProjectStatus status) {

        List<Project> allProjects = projectRepository.findAll();

        List<Project> filteredProjects = allProjects.stream()
                .filter(project -> name == null || project.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(project -> location == null
                        || project.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(project -> type == null || project.getType() == type)
                .filter(project -> status == null || project.getStatus() == status)
                .collect(Collectors.toList());

        filteredProjects.forEach(this::populateOwner);
        return filteredProjects;
    }

    private void populateOwner(Project project) {
        if (project.getIssuerId() != null) {
            userRepository.findById(project.getIssuerId()).ifPresent(user -> {
                // Create a safe user object to avoid exposing password/roles if not needed
                // Or just rely on JsonIgnore in User model (which exists for password)
                project.setOwner(user);
            });
        }
    }
}