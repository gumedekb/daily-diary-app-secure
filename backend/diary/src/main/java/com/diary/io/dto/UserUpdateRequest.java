package com.diary.io.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Input DTO for updating the currently authenticated user. Only these fields
 * are bindable, which prevents mass assignment of protected fields such as
 * {@code id} or {@code createdAt}. Null fields are left unchanged.
 */
public class UserUpdateRequest {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Email(message = "A valid email is required")
    @Size(max = 100, message = "Email must be at most 100 characters")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
