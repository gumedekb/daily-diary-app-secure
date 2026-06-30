package com.diary.io.user;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.diary.io.dto.UserResponse;
import com.diary.io.dto.UserUpdateRequest;
import com.diary.io.exception.ResourceNotFoundException;
import com.diary.io.security.UserPrincipal;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userServ) {
		this.userService = userServ;
	}

	// NOTE: user creation is handled exclusively by POST /api/auth/signup, which
	// hashes the password. The previously public POST /api/users endpoint was
	// removed because it stored credentials unauthenticated and unhashed.

	@GetMapping("/{id}")
	public ResponseEntity<UserResponse> getUserById(@PathVariable Long id,
			@AuthenticationPrincipal UserPrincipal currentUser) {
		requireSelf(id, currentUser);
		return userService.getUserById(id)
	            .map(UserResponse::from)
	            .map(ResponseEntity::ok)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	@PutMapping("/{id}")
	public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
			@Valid @RequestBody UserUpdateRequest userDetails,
			@AuthenticationPrincipal UserPrincipal currentUser) {
		requireSelf(id, currentUser);
		return userService.updateUser(id, userDetails)
	            .map(UserResponse::from)
	            .map(ResponseEntity::ok)
	            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
	}

	/** Ensures a user can only read/modify their own account. */
	private void requireSelf(Long id, UserPrincipal currentUser) {
		if (currentUser == null || !currentUser.getId().equals(id)) {
			throw new AccessDeniedException("You can only access your own account.");
		}
	}

}
