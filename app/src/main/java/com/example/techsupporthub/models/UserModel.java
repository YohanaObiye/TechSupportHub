package com.example.techsupporthub.models;

public class UserModel {
    private String id;
    private String username;
    private String email;
    private String role;
    private long createdAt;
    private String category; // New field for technician category
    private String about;    // New field for technician description
    private String profile;    // New field for technician description

    public UserModel() {
    }

    public UserModel(String id, String username, String email, String role, long createdAt, String category, String about, String profile) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.category = category;
        this.about = about;
        this.profile = profile;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getCategory() {
        return category;
    }

    public String getAbout() {
        return about;
    }

    public String getProfile() {
        return profile;
    }
}
