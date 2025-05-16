package com.must.courseevaluation.security;

import org.springframework.stereotype.Component;

@Component("userSecurity")
public class UserSecurity {
    
    public boolean isUserSelf(Long userId, UserDetailsImpl principal) {
        return principal != null && principal.getId().equals(userId);
    }
} 