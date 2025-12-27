package dev.snds_prfct.orders.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PrincipalUtils {

    public static Long getCurrentUserId() {
        return 1L; // Simulates getting the current user's ID from the App security context for simplicity
    }
}
