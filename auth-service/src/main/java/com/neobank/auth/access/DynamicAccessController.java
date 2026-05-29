package com.neobank.auth.access;

import org.springframework.stereotype.Service;
import java.time.LocalTime;
import java.util.*;

@Service
public class DynamicAccessController {
    private final List<AccessPolicy> policies = List.of(
            new TimeBasedPolicy(),
            new LocationBasedPolicy(),
            new RoleBasedPolicy()
    );

    public AccessDecision checkAccess(AccessRequest request) {
        for (AccessPolicy policy : policies) {
            if (!policy.evaluate(request)) {
                return AccessDecision.DENY.withReason(policy.getDenialReason());
            }
        }
        return AccessDecision.ALLOW;
    }
}

interface AccessPolicy {
    boolean evaluate(AccessRequest request);
    String getDenialReason();
}

class TimeBasedPolicy implements AccessPolicy {
    @Override
    public boolean evaluate(AccessRequest request) {
        LocalTime now = LocalTime.now();
        LocalTime start = LocalTime.of(9, 0);
        LocalTime end = LocalTime.of(17, 0);

        if (request.user().hasRole("VIP")) return true;
        return now.isAfter(start) && now.isBefore(end);
    }
    @Override
    public String getDenialReason() {
        return "Outside business hours";
    }
}

class LocationBasedPolicy implements AccessPolicy {
    private final Set<String> blockedCountries = Set.of("XX", "YY");

    @Override
    public boolean evaluate(AccessRequest request) {
        return !blockedCountries.contains(request.country());
    }

    @Override
    public String getDenialReason() {
        return "Access from blocked country";
    }
}

class RoleBasedPolicy implements AccessPolicy {
    @Override
    public boolean evaluate(AccessRequest request) {
        Set<String> allowedRoles = Set.of("ADMIN", "MANAGER", "VIP");
        return request.user().roles().stream().anyMatch(allowedRoles::contains);
    }

    @Override
    public String getDenialReason() {
        return "Insufficient role";
    }
}

// Fixed Record Definitions
record User(String id, Set<String> roles) {
    // Constructor with validation
    public User {
        roles = roles == null ? Set.of() : roles;
    }

    // Method to check role
    public boolean hasRole(String role) {
        return roles.contains(role);
    }
}

record AccessRequest(User user, String resource, String action, String country) {}

record AccessDecision(boolean allowed, String reason) {
    static final AccessDecision ALLOW = new AccessDecision(true, null);
    static final AccessDecision DENY = new AccessDecision(false, null);

    AccessDecision withReason(String reason) {
        return new AccessDecision(false, reason);
    }
}