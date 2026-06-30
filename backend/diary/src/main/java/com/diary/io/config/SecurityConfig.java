package com.diary.io.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import com.diary.io.security.CustomUserDetailsService;
import com.diary.io.security.JwtAuthenticationFilter;
import com.diary.io.security.JwtProvider;
import com.diary.io.security.TokenBlacklist;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

	private final JwtProvider jwtProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final TokenBlacklist tokenBlacklist;

    public SecurityConfig(JwtProvider jwtProvider, CustomUserDetailsService customUserDetailsService,
            TokenBlacklist tokenBlacklist) {
        this.jwtProvider = jwtProvider;
        this.customUserDetailsService = customUserDetailsService;
        this.tokenBlacklist = tokenBlacklist;
    }

	@Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
	    return new JwtAuthenticationFilter(jwtProvider, customUserDetailsService, tokenBlacklist);
	}

	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http
		    .csrf(csrf -> csrf.disable())
		    // Stateless: never create or rely on an HTTP session, every request
		    // is authenticated from the bearer token alone.
		    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
		    .authorizeHttpRequests(auth -> auth
		        // Only signup/login are public. logout requires a valid token.
		        .requestMatchers("/api/auth/signup", "/api/auth/login").permitAll()
		        // Everything else (diary, users, logout, ...) must be authenticated.
		        // Deny-by-default replaces the previous anyRequest().permitAll().
		        .anyRequest().authenticated()
		    )
		    // Return 401 instead of redirecting to a login form for unauthenticated API calls.
		    .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
		    .formLogin(login -> login.disable())
		    .httpBasic(basic -> basic.disable());

		http.cors(cors -> cors.configurationSource(request -> {
		    CorsConfiguration config = new CorsConfiguration();
		    config.setAllowedOrigins(List.of("http://localhost:5173"));
		    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		    config.setAllowCredentials(true);
		    return config;
		}));


		// JWT filter
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
