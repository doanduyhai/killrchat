package com.datastax.demo.killrchat.security.utils;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.exceptions.UserNotFoundException;
import com.datastax.demo.killrchat.security.authority.CustomUserDetails;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@RunWith(MockitoJUnitRunner.class)
public class SecurityUtilsTest {

    @Test
    public void should_return_a_login_from_user_details() throws Exception {
        //Given
        final UserDetails userDetails = new CustomUserDetails(Sets.<GrantedAuthority>newHashSet(),"emc2","a.einstein");
        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return userDetails;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "emc2";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //When
        final String currentLogin = SecurityUtils.getCurrentLogin();

        //Then
        assertThat(currentLogin).isEqualTo("emc2");
    }

    @Test
    public void should_return_a_login_from_string() throws Exception {
        //Given
        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return "emc2";
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "emc2";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //When
        final String currentLogin = SecurityUtils.getCurrentLogin();

        //Then
        assertThat(currentLogin).isEqualTo("emc2");
    }

    @Test(expected = UserNotFoundException.class)
    public void should_throw_exception_when_no_principal() throws Exception {
        //Given
        final Authentication authentication = new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return true;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {

            }

            @Override
            public String getName() {
                return "emc2";
            }
        };
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //When
        SecurityUtils.getCurrentLogin();
    }

    @Test(expected = UserNotFoundException.class)
    public void should_throw_exception_when_no_security_context() throws Exception {
        SecurityUtils.getCurrentLogin();
    }
}