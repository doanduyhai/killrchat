package com.datastax.demo.killrchat.security.authority;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;

@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public class UserAuthority implements GrantedAuthority {
    @Override
    public String getAuthority() {
        return AuthoritiesConstants.USER;
    }
}
