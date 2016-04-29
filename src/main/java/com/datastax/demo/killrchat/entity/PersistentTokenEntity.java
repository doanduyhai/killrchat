package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.security.authority.UserAuthority;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import info.archinnov.achilles.annotations.*;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.PERSISTENT_TOKEN;


@Table(keyspace = KEYSPACE, table = PERSISTENT_TOKEN)
@TTL(value = PersistentTokenEntity.TOKEN_VALIDITY_SECONDS)
public class PersistentTokenEntity implements Serializable {

    public static final int TOKEN_VALIDITY_DAYS = 31;
    public static final int TOKEN_VALIDITY_SECONDS = 60 * 60 * 24 * TOKEN_VALIDITY_DAYS;

    private static final SimpleDateFormat DATE_TIME_FORMATTER = new SimpleDateFormat("YYYY-MM-dd");

    private static final int MAX_USER_AGENT_LEN = 255;

    @PartitionKey
    private String series;

    @JsonIgnore
    @NotNull
    @Column("token_value")
    private String tokenValue;

    @JsonIgnore
    @Column("token_date")
    private Date tokenDate;

    //an IPV6 address max length is 39 characters
    @Size(min = 0, max = 39)
    @Column("ip_address")
    private String ipAddress;

    @Column("user_agent")
    private String userAgent;

    @Column
    private String login;

    @Column
    private String pass;

    @EmptyCollectionIfNull
    @Column
    private Set<@Frozen UserAuthority> authorities = new HashSet<>();

    public PersistentTokenEntity() {
    }

    public PersistentTokenEntity(String series, String tokenValue, Date tokenDate, String ipAddress, String userAgent, String login, String pass, @EmptyCollectionIfNull Set<@Frozen UserAuthority> authorities) {
        this.series = series;
        this.tokenValue = tokenValue;
        this.tokenDate = tokenDate;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.login = login;
        this.pass = pass;
        this.authorities = authorities;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public Date getTokenDate() {
        return tokenDate;
    }

    public void setTokenDate(Date tokenDate) {
        this.tokenDate = tokenDate;
    }

    @JsonGetter
    public String getFormattedTokenDate() {
        return DATE_TIME_FORMATTER.format(this.tokenDate);
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        if (userAgent.length() >= MAX_USER_AGENT_LEN) {
            this.userAgent = userAgent.substring(0, MAX_USER_AGENT_LEN - 1);
        } else {
            this.userAgent = userAgent;
        }
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPass() {
        return pass;
    }

    public void setPass(String password) {
        this.pass = password;
    }

    public Set<UserAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Set<UserAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PersistentTokenEntity that = (PersistentTokenEntity) o;

        if (!series.equals(that.series)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return series.hashCode();
    }

    @Override
    public String toString() {
        return "PersistentToken{" +
                "series='" + series + '\'' +
                ", tokenValue='" + tokenValue + '\'' +
                ", tokenDate=" + tokenDate +
                ", ipAddress='" + ipAddress + '\'' +
                ", userAgent='" + userAgent + '\'' +
                "}";
    }
}
