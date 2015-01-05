package com.datastax.demo.killrchat.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.ChatRoomModel.Action;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomCreationModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomDeletionModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel.Status;
import com.datastax.demo.killrchat.service.ChatRoomService;
import com.datastax.demo.killrchat.service.MessageService;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collection;
import java.util.Date;
import java.util.Set;


@RunWith(MockitoJUnitRunner.class)
public class ChatRoomResourceTest {

    @InjectMocks
    private ChatRoomResource resource;

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private MessageService messageService;

    @Mock
    private SimpMessagingTemplate template;

    private LightUserModel john = new LightUserModel("jdoe", "John", "DOE");

    @Test
    public void should_create_chat_room() throws Exception {
        //Given
        String roomName = "games";
        final LightUserModel userModel = new LightUserModel("jdoe", "John", "DOE");

        //When
        resource.createChatRoom(roomName, new ChatRoomCreationModel("banner", userModel));

        //Then
        verify(chatRoomService).createChatRoom(roomName, "banner", userModel);
    }

    @Test
    public void should_find_room_by_name() throws Exception {
        //Given
        final Date now = new Date();
        final ChatRoomModel roomModel = new ChatRoomModel("games",john, now, "banner", Sets.<LightUserModel>newHashSet());
        when(chatRoomService.findRoomByName("games")).thenReturn(roomModel);

        //When
        final ChatRoomModel found = resource.findRoomByName("games");

        //Then
        assertThat(found).isSameAs(roomModel);
    }

    @Test
    public void should_list_chat_rooms_from_lower_bound_with_page_size() throws Exception {
        //Given

        //When
        resource.listChatRooms(11);

        //Then
        verify(chatRoomService).listChatRooms(11);
    }

    @Test
    public void should_add_user_to_chat_room() throws Exception {
        //Given
        final MessageModel joiningMessage = new MessageModel();
        when(messageService.createJoiningMessage("games", john)).thenReturn(joiningMessage);

        //When
        resource.addUserToChatRoom("games", john);

        //Then
        verify(chatRoomService).addUserToRoom("games", john);
        verify(template).convertAndSend("/topic/participants/games", john, ImmutableMap.<String, Object>of("status", Status.JOIN));
        verify(template).convertAndSend("/topic/messages/games", joiningMessage);
    }

    @Test
    public void should_remove_user_from_chat_room() throws Exception {
        //Given
        final MessageModel leavingMessage = new MessageModel();
        when(messageService.createLeavingMessage("games", john)).thenReturn(leavingMessage);

        //When
        resource.removeUserFromChatRoom("games", john);

        //Then
        verify(chatRoomService).removeUserFromRoom("games", john);
        verify(template).convertAndSend("/topic/participants/games", john, ImmutableMap.<String, Object>of("status", Status.LEAVE));
        verify(template).convertAndSend("/topic/messages/games", leavingMessage);
    }

    @Test
    public void should_delete_chat_room_with_participants() throws Exception {
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
                return "jdoe";
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
                return "jdoe";
            }
        };

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Set<LightUserModel> participants = Sets.newHashSet(john);

        when(chatRoomService.deleteRoomWithParticipants("jdoe", "games", participants)).thenReturn("message");

        //When
        resource.deleteRoomWithParticipants("games", new ChatRoomDeletionModel(participants));

        //Then
        verify(template).convertAndSend("/topic/action/games", "message", ImmutableMap.<String,Object>of("action", Action.DELETE, "room", "games", "creator", "jdoe"));
    }
}