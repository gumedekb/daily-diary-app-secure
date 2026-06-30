package com.diary.io.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
        this.authService = authService;
    }

	@PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

	@PostMapping("/login")
	public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
	    return authService.login(request, clientKey(httpRequest, request.getUsername()));
	}

	@PostMapping("/logout")
	public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
	    String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;
	    return authService.logout(token);
	}

	/** Builds the rate-limit key from the caller's IP and the attempted username. */
	private String clientKey(HttpServletRequest request, String username) {
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip == null || ip.isBlank()) {
	        ip = request.getRemoteAddr();
	    }
	    return ip + "|" + (username == null ? "" : username.toLowerCase());
	}

}
