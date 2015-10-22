package com.datastax.demo.killrchat.security.authority;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import info.archinnov.achilles.annotations.Column;
import info.archinnov.achilles.annotations.UDT;
import org.springframework.security.core.GrantedAuthority;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USER_AUTHORITY_UDT;

@UDT(keyspace = KEYSPACE, name = USER_AUTHORITY_UDT)
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public class UserAuthority implements GrantedAuthority {
    @Column
    private String authority = AuthoritiesConstants.USER;

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
