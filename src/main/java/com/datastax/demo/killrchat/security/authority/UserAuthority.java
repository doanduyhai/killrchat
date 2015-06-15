package com.datastax.demo.killrchat.security.authority;

import com.datastax.demo.killrchat.entity.Schema;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.GrantedAuthority;

@UDT(keyspace = Schema.KEYSPACE, name = Schema.USER_AUTHORITY_UDT)
@JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="@class")
public class UserAuthority implements GrantedAuthority {
    @Field
    private String authority = AuthoritiesConstants.USER;

    @Override
    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }
}
