package com.carboncredit.controller;

import com.carboncredit.model.Project;
import com.carboncredit.model.User;
import com.carboncredit.model.Verification;
import com.carboncredit.repository.*;
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

public class AdminControllerTest {

    @InjectMocks
    private AdminController adminController;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CarbonCreditRepository carbonCreditRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testVerifyProject_Approved() {
        String projectId = "proj1";
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("status", "APPROVED");

        Project project = new Project();
        project.setId(projectId);
        project.setStatus(Project.ProjectStatus.PENDING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(verificationRepository.save(any(Verification.class))).thenReturn(new Verification());

        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        UserDetailsImpl userDetails = new UserDetailsImpl("admin1", "admin@test.com", "password", new HashSet<>());
        when(authentication.getPrincipal()).thenReturn(userDetails);

        ResponseEntity<?> response = adminController.verifyProject(projectId, requestBody);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(Project.ProjectStatus.VERIFIED, project.getStatus());
        verify(projectRepository, times(1)).save(project);
        verify(verificationRepository, times(1)).save(any(Verification.class));
    }

    @Test
    public void testGetSystemStats() {
        when(userRepository.count()).thenReturn(10L);
        when(projectRepository.count()).thenReturn(5L);
        when(carbonCreditRepository.count()).thenReturn(100L);
        when(orderRepository.count()).thenReturn(20L);
        when(tradeRepository.count()).thenReturn(15L);

        ResponseEntity<?> response = adminController.getSystemStats();

        assertEquals(200, response.getStatusCode().value());
        Map<String, Object> stats = (Map<String, Object>) response.getBody();
        assertEquals(10L, stats.get("totalUsers"));
        assertEquals(5L, stats.get("totalProjects"));
    }

    @Test
    public void testUpdateUser() {
        String userId = "user1";
        User user = new User();
        user.setId(userId);
        user.setName("Old Name");

        User userDetails = new User();
        userDetails.setName("New Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<User> response = adminController.updateUser(userId, userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("New Name", user.getName());
        verify(userRepository, times(1)).save(user);
    }
}
