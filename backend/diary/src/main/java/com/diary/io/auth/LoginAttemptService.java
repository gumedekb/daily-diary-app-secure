package com.diary.io.auth;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

/**
 * Simple in-memory rate limiter / lockout for authentication attempts.
 *
 * A key (the client IP, or "ip|username") is locked out after
 * {@value #MAX_ATTEMPTS} failed attempts and stays locked for
 * {@link #LOCK_DURATION}. A successful login clears the counter.
 *
 * This is intentionally dependency-free. For a multi-instance deployment back
 * it with a shared store (e.g. Redis) or a dedicated library such as Bucket4j.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private static final class Attempt {
        final AtomicInteger count = new AtomicInteger(0);
        volatile Instant lockedUntil = Instant.EPOCH;
    }

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        Attempt attempt = attempts.get(key);
        return attempt != null && Instant.now().isBefore(attempt.lockedUntil);
    }

    public void loginFailed(String key) {
        Attempt attempt = attempts.computeIfAbsent(key, k -> new Attempt());
        int current = attempt.count.incrementAndGet();
        if (current >= MAX_ATTEMPTS) {
            attempt.lockedUntil = Instant.now().plus(LOCK_DURATION);
        }
    }

    public void loginSucceeded(String key) {
        attempts.remove(key);
    }
}
