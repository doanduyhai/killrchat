package com.datastax.demo.killrchat.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserModel extends LightUserModel {

    @Size(max = 100)
    @Pattern(regexp="[a-zA-Z0-9][a-zA-Z0-9_.-]{2,30}")
    protected String nickname;

    @NotEmpty
    private String password;

    @Email
    private String email;

    @Size(max = 2000)
    private String bio;

    private Set<String> chatRooms;

//    public UserModel(String login, String password, String firstname, String lastname, String email, String bio, Set<String> chatRooms) {
//        super(login, firstname, lastname);
//        this.password = password;
//        this.email = email;
//        this.bio = bio;
//        this.chatRooms = chatRooms;
//    }

    public UserModel(String login, String password, String firstname, String lastname, String email, String bio) {
        super(login, firstname, lastname);
        this.password = password;
        this.email = email;
        this.bio = bio;
    }

    public LightUserModel toLightModel() {
        return new LightUserModel(this.login, this.firstname, this.lastname);
    }

    public UserModel() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UserModel that = (UserModel) o;

        return  Objects.equals(this.login,that.login);
    }

    @Override
    public int hashCode() {
       return Objects.hash(this.login);
    }
}
