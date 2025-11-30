package com.carboncredit.repository;

import com.carboncredit.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByIssuerId(String issuerId);
    List<Project> findByStatus(Project.ProjectStatus status);
}