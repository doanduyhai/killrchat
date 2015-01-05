package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping("/remember-me")
public class RememberMeResource {


    @Inject
    private UserService service;

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserModel getRememberMeUser() {
        return service.fetchRememberMeUser();
    }

}
