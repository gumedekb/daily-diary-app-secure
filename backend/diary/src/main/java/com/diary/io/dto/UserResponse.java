package com.diary.io.dto;

import java.time.LocalDateTime;

import com.diary.io.user.User;

/** Output DTO for users; never exposes the password hash. */
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse dto = new UserResponse();
        dto.id = user.getId();
        dto.username = user.getUsername();
        dto.email = user.getEmail();
        dto.createdAt = user.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
