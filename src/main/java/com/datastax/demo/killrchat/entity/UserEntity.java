package com.datastax.demo.killrchat.entity;

import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.security.authority.CustomUserDetails;
import com.datastax.demo.killrchat.security.authority.UserAuthority;
import com.google.common.collect.Sets;
import info.archinnov.achilles.annotations.*;
import info.archinnov.achilles.type.NamingStrategy;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.Set;

import static com.datastax.demo.killrchat.entity.Schema.KEYSPACE;
import static com.datastax.demo.killrchat.entity.Schema.USERS;

@Entity(keyspace = KEYSPACE, table = USERS, comment = "users table")
@Strategy(naming = NamingStrategy.SNAKE_CASE)
public class UserEntity {

    @PartitionKey
    private String login;

    @NotEmpty
    @Column
    private String pass;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private String email;

    @Column
    private String bio;

    @EmptyCollectionIfNull
    @Column
    private Set<String> chatRooms;

    public UserEntity(String login, String pass, String firstname, String lastname, String email, String bio) {
        this.login = login;
        this.pass = pass;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.bio = bio;
    }

    public static UserEntity fromModel(UserModel model) {
        final UserEntity userEntity = new UserEntity();
        userEntity.setLogin(model.getLogin());
        userEntity.setPass(model.getPassword());
        userEntity.setFirstname(model.getFirstname());
        userEntity.setLastname(model.getLastname());
        userEntity.setEmail(model.getEmail());
        userEntity.setBio(model.getBio());
        return userEntity;
    }


    public UserModel toModel() {
        final UserModel model = new UserModel();
        model.setLogin(this.getLogin());
        model.setFirstname(this.getFirstname());
        model.setLastname(this.getLastname());
        model.setEmail(this.getEmail());
        model.setBio(this.getBio());
        model.setChatRooms(this.getChatRooms());
        return model;
    }

    public CustomUserDetails toUserDetails() {
        return new CustomUserDetails(Sets.newHashSet(new UserAuthority()),this.login,this.pass);
    }


    /**
     *
     * Boring getters & setters & default constructor
     *
     */
    public UserEntity() {
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

    public void setPass(String pass) {
        this.pass = pass;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public Set<String> getChatRooms() {
        return chatRooms;
    }

    public void setChatRooms(Set<String> chatRooms) {
        this.chatRooms = chatRooms;
    }
}
