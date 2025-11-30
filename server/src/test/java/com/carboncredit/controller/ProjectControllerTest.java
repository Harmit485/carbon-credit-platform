package com.carboncredit.controller;

import com.carboncredit.model.Project;
import com.carboncredit.repository.ProjectRepository;
import com.carboncredit.service.CreditService;
import com.carboncredit.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProjectControllerTest {

    @InjectMocks
    private ProjectController projectController;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private CreditService creditService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateProject() {
        Project project = new Project();
        project.setName("Test Project");

        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "user@test.com", "password", new HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(projectRepository.save(any(Project.class))).thenReturn(project);

        Project createdProject = projectController.createProject(project);

        assertEquals("user1", createdProject.getIssuerId());
        assertEquals(Project.ProjectStatus.PENDING, createdProject.getStatus());
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    public void testVerifyProject_GeneratesCredits() {
        String projectId = "proj1";
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "VERIFIED");

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(Project.ProjectStatus.PENDING);
        project.setType(Project.ProjectType.SOLAR); // Reducing project
        project.setTotalCarbonCredits(100.0);
        project.setIssuerId("user1");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);

        ResponseEntity<Project> response = projectController.verifyProject(projectId, statusUpdate);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Project.ProjectStatus.VERIFIED, project.getStatus());
        verify(creditService, times(1)).generateCredits(projectId, 100.0, "user1");
        verify(projectRepository, times(1)).save(project);
    }

    @Test
    public void testGetMyProjects() {
        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("user1", "user@test.com", "password", new HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(projectRepository.findByIssuerId("user1")).thenReturn(Collections.emptyList());

        List<Project> projects = projectController.getMyProjects();
        verify(projectRepository, times(1)).findByIssuerId("user1");
    }
}
