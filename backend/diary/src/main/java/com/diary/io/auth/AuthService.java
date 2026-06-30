package com.diary.io.auth;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.diary.io.security.JwtProvider;
import com.diary.io.security.TokenBlacklist;
import com.diary.io.user.User;
import com.diary.io.user.UserService;

@Service
public class AuthService {

	private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtProvider jwtProvider;
    private final LoginAttemptService loginAttemptService;
    private final TokenBlacklist tokenBlacklist;

    public AuthService(UserService userService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtProvider jwtProvider,
            LoginAttemptService loginAttemptService,
            TokenBlacklist tokenBlacklist) {
		this.userService = userService;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtProvider = jwtProvider;
		this.loginAttemptService = loginAttemptService;
		this.tokenBlacklist = tokenBlacklist;
	}

    public ResponseEntity<?> signup(SignupRequest request) {
        // Check if user exists
        if (userService.userExists(request.getEmail(), request.getUsername())) {
            return ResponseEntity.badRequest().body("Username or email already taken.");
        }

        // Create new user with encoded password
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        userService.saveUser(user);
        return ResponseEntity.ok("User registered successfully.");
    }

    public ResponseEntity<?> login(LoginRequest request, String clientKey) {
        // Brute-force protection: refuse further attempts once locked out.
        if (loginAttemptService.isBlocked(clientKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many failed login attempts. Please try again later.");
        }

        try {
            // Authenticate user (never log the raw credentials).
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
                )
            );

            loginAttemptService.loginSucceeded(clientKey);

            String token = jwtProvider.generateToken(authentication.getName());

            LoginResponse response = new LoginResponse();
			response.setMessage("User authenticated successfully.");
			response.setUsername(authentication.getName());
			response.setToken(token);

			return ResponseEntity.ok(response);

        } catch (AuthenticationException e) {
            loginAttemptService.loginFailed(clientKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username/email or password");
        }
    }

    /**
     * Invalidates the supplied token server-side so it can no longer be used,
     * even though it has not yet expired.
     */
    public ResponseEntity<?> logout(String token) {
        if (token != null && jwtProvider.validateToken(token)) {
            tokenBlacklist.blacklist(token, jwtProvider.getExpirationFromToken(token));
        }
        return ResponseEntity.ok("Logged out successfully.");
    }

}
