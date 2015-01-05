package com.datastax.demo.killrchat.configuration;

import com.datastax.demo.killrchat.security.*;
import com.datastax.demo.killrchat.security.handlers.AjaxAuthenticationFailureHandler;
import com.datastax.demo.killrchat.security.handlers.AjaxAuthenticationSuccessHandler;
import com.datastax.demo.killrchat.security.handlers.AjaxLogoutSuccessHandler;
import com.datastax.demo.killrchat.security.service.CustomPersistentRememberMeService;
import com.datastax.demo.killrchat.security.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Inject
    private Environment env;

    @Inject
    private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

    @Inject
    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

    @Inject
    private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

    @Inject
    private Http401UnauthorizedEntryPoint authenticationEntryPoint;

    @Inject
    private CustomUserDetailsService userDetailsService;

    @Inject
    private CustomPersistentRememberMeService rememberMeService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Inject
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
            .antMatchers("/bower_components/**")
            .antMatchers("/fonts/**")
            .antMatchers("/images/**")
            .antMatchers("/views/login**")
            .antMatchers("/scripts/**")
            .antMatchers("/styles/**");

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .exceptionHandling()
            .authenticationEntryPoint(authenticationEntryPoint)
        .and()
            .rememberMe()
            .rememberMeServices(rememberMeService)
            .key(env.getProperty("killrchat.security.rememberme.key"))
        .and()
            .formLogin()
            .loginProcessingUrl("/authenticate")
                .usernameParameter("j_username")
                .passwordParameter("j_password")
                .successHandler(ajaxAuthenticationSuccessHandler)
                .failureHandler(ajaxAuthenticationFailureHandler)
            .permitAll()
        .and()
            .logout()
            .logoutUrl("/logout")
            .logoutSuccessHandler(ajaxLogoutSuccessHandler)
            .deleteCookies("JSESSIONID")
            .permitAll()
        .and()
            .csrf()
            .disable()
            .headers()
            .xssProtection()
            .frameOptions()
            .disable()
            .authorizeRequests()
                .antMatchers("/users").permitAll()
                .antMatchers("/security/remember-me").permitAll()
                .antMatchers("/authenticate").permitAll()
                .antMatchers("/chat/**").authenticated()
                .antMatchers("/views/chat**").authenticated()
                .antMatchers("/users/**").authenticated()
                .antMatchers("/rooms/**").authenticated()
                .antMatchers("/rooms").authenticated()
                .antMatchers("/messages/**").authenticated();

    }
}
