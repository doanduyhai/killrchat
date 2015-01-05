package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.ChatRoomModel;
import com.datastax.demo.killrchat.model.ChatRoomModel.Action;
import com.datastax.demo.killrchat.model.LightUserModel;
import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomCreationModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomDeletionModel;
import com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel;
import com.datastax.demo.killrchat.security.utils.SecurityUtils;
import com.datastax.demo.killrchat.service.ChatRoomService;
import com.datastax.demo.killrchat.service.MessageService;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.datastax.demo.killrchat.resource.model.ChatRoomParticipantModel.Status;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/rooms")
public class ChatRoomResource {

    public static final int DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE = 100;

    @Inject
    private ChatRoomService chatRoomService;

    @Inject
    private MessageService messageService;

    @Inject
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/{roomName}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createChatRoom(@PathVariable @NotEmpty @Pattern(regexp="[a-zA-Z0-9][a-zA-Z0-9_.-]{2,30}") String roomName, @NotNull @RequestBody @Valid ChatRoomCreationModel model) {
        final LightUserModel creator = model.getCreator();

        chatRoomService.createChatRoom(roomName, model.getBanner(), creator);
    }

    @RequestMapping(value = "/{roomName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public ChatRoomModel findRoomByName(@PathVariable @NotEmpty String roomName) {
        return chatRoomService.findRoomByName(roomName);
    }

    @RequestMapping(value = "/{roomName}", method = PATCH, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoomWithParticipants(@PathVariable @NotEmpty String roomName, @RequestBody @Valid ChatRoomDeletionModel model) {
        final String login = SecurityUtils.getCurrentLogin();
        final String deletionMessage = chatRoomService.deleteRoomWithParticipants(login, roomName, model.getParticipants());
        template.convertAndSend("/topic/action/"+roomName, deletionMessage, ImmutableMap.<String,Object>of("action", Action.DELETE, "room", roomName, "creator", login));
    }

    @RequestMapping(method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<ChatRoomModel> listChatRooms(@RequestParam(required = false) int fetchSize) {
        final int pageSize = fetchSize <= 0 ? DEFAULT_CHAT_ROOMS_LIST_FETCH_SIZE : fetchSize;
        return chatRoomService.listChatRooms(pageSize);
    }

    @RequestMapping(value = "/participant/{roomName}", method = PUT, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUserToChatRoom(@PathVariable @NotEmpty String roomName, @NotNull @RequestBody @Valid LightUserModel participant) {
        chatRoomService.addUserToRoom(roomName, participant);
        final MessageModel joiningMessage = messageService.createJoiningMessage(roomName, participant);
        template.convertAndSend("/topic/participants/"+ roomName, participant, ImmutableMap.<String,Object>of("status", Status.JOIN));
        template.convertAndSend("/topic/messages/"+roomName, joiningMessage);
    }

    @RequestMapping(value = "/participant/{roomName}", method = PATCH, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserFromChatRoom(@PathVariable @NotEmpty String roomName, @NotNull @RequestBody @Valid LightUserModel participant) {
        chatRoomService.removeUserFromRoom(roomName, participant);
        final MessageModel leavingMessage = messageService.createLeavingMessage(roomName, participant);
        template.convertAndSend("/topic/participants/"+ roomName, participant, ImmutableMap.<String,Object>of("status", Status.LEAVE));
        template.convertAndSend("/topic/messages/"+roomName, leavingMessage);
    }
}
