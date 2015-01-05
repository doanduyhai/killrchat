package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/users")
public class UserResource {

    @Inject
    private UserService service;

    @Inject
    private PasswordEncoder passwordEncoder;

    @RequestMapping(method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createUser(@NotNull @RequestBody @Valid UserModel model) {
        model.setPassword(passwordEncoder.encode(model.getPassword()));
        service.createUser(model);
    }

    @RequestMapping(value = "/{login:.+}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public UserModel findByLogin(@PathVariable String login) {
        return service.findByLogin(login).toModel();
    }

}
