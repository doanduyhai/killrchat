package com.datastax.demo.killrchat.resource;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.model.UserModel;
import com.datastax.demo.killrchat.service.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RememberMeResourceTest {

    @InjectMocks
    private RememberMeResource resource;

    @Mock
    private UserService service;

    @Test
    public void should_fetch_user_from_security_context() throws Exception {
        //Given
        UserModel model = new UserModel();
        when(service.fetchRememberMeUser()).thenReturn(model);

        //When
        final UserModel actual = resource.getRememberMeUser();

        //Then
        assertThat(actual).isSameAs(model);
    }
}