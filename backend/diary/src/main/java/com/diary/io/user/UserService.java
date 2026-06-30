package com.diary.io.user;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.diary.io.dto.UserUpdateRequest;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	// Constructor injection preferred
	public UserService(UserRepository userRep, PasswordEncoder passwordEncoder) {
		this.userRepository = userRep;
		this.passwordEncoder = passwordEncoder;
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByUsername(username);
	}

	public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

	public User saveUser(User user) {
        return userRepository.save(user);
    }

	public boolean userExists(String email, String username) {
        return userRepository.findByEmail(email).isPresent() || userRepository.findByUsername(username).isPresent();
    }

	// Get user by ID
	public Optional<User> getUserById(Long id) {
	    return userRepository.findById(id);
	}

	// Update user — only the supplied fields change, and the password is always
	// re-hashed (never stored in clear text).
	public Optional<User> updateUser(Long id, UserUpdateRequest userDetails) {
	    return userRepository.findById(id).map(user -> {
	        if (StringUtils.hasText(userDetails.getUsername())) {
	            user.setUsername(userDetails.getUsername());
	        }
	        if (StringUtils.hasText(userDetails.getEmail())) {
	            user.setEmail(userDetails.getEmail());
	        }
	        if (StringUtils.hasText(userDetails.getPassword())) {
	            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
	        }
	        return userRepository.save(user);
	    });
	}

}
