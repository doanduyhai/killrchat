package com.datastax.demo.killrchat.resource.model;

import com.datastax.demo.killrchat.model.LightUserModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePosting {

    @NotNull
    private LightUserModel author;

    @NotBlank
    private String content;

    public MessagePosting() {
    }

    public MessagePosting(LightUserModel author, String content) {
        this.author = author;
        this.content = content;
    }

    public LightUserModel getAuthor() {
        return author;
    }

    public void setAuthor(LightUserModel author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
