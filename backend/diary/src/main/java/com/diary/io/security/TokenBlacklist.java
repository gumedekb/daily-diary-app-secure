package com.diary.io.security;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * In-memory blacklist of JWTs that have been explicitly invalidated (e.g. on
 * logout). Because the JWTs are otherwise stateless, this gives us true
 * server-side revocation: a blacklisted token is rejected by the auth filter
 * even though its signature and expiry are still valid.
 *
 * Note: this store is per-instance. For a multi-instance deployment back it
 * with a shared store such as Redis.
 */
@Component
public class TokenBlacklist {

    // token -> original expiry. We keep entries only until the token would
    // have expired on its own, then drop them so the map cannot grow forever.
    private final Map<String, Date> revoked = new ConcurrentHashMap<>();

    public void blacklist(String token, Date expiry) {
        if (token != null) {
            revoked.put(token, expiry != null ? expiry : new Date());
        }
    }

    public boolean isBlacklisted(String token) {
        return token != null && revoked.containsKey(token);
    }

    /** Periodically purge tokens that have passed their natural expiry. */
    @Scheduled(fixedDelay = 3600000) // hourly
    public void purgeExpired() {
        Date now = new Date();
        revoked.entrySet().removeIf(entry -> entry.getValue().before(now));
    }
}
