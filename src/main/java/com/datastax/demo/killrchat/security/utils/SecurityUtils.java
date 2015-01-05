package com.datastax.demo.killrchat.security.utils;

import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


/**
 * Utility class for Spring Security.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * Get the login of the current user.
     */
    public static String getCurrentLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();

        if(authentication != null) {
            if (authentication.getPrincipal() instanceof UserDetails) {
                return  ((UserDetails) authentication.getPrincipal()).getUsername();
            } else if (authentication.getPrincipal() instanceof String) {
                return (String) authentication.getPrincipal();
            } else {
                throw new UserNotFoundException("Cannot find user from security context");
            }
        }
        throw new UserNotFoundException("Cannot find user from security context");
    }
}
