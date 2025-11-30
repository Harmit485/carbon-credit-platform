package com.carboncredit.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {
    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String email;

    @com.fasterxml.jackson.annotation.JsonIgnore
    private String password;

    private Set<Role> roles = new HashSet<>();

    private String organization;

    private String country;

    private boolean verified = false;

    private String profileImage;

    private String description;

    private String website;

    public enum Role {
        ROLE_ADMIN,
        ROLE_USER
    }
}