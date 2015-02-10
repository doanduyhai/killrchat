package com.datastax.demo.killrchat.resource;

import com.datastax.demo.killrchat.model.MessageModel;
import com.datastax.demo.killrchat.resource.model.MessagePosting;
import com.datastax.demo.killrchat.service.MessageService;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/messages")
public class MessageResource {

    static final int DEFAULT_MESSAGES_FETCH_SIZE = 5;

    @Inject
    private MessageService service;

    @Inject
    private SimpMessagingTemplate template;

    @RequestMapping(value = "/{roomName}", method = POST, consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void postNewMessage(@PathVariable String roomName, @NotNull @RequestBody @Valid MessagePosting messagePosting) throws JsonProcessingException {
        Validator.validateNotBlank(roomName, "Room name can not be blank for posting new message");
        final MessageModel messageModel = service.postNewMessage(messagePosting.getAuthor(), roomName, messagePosting.getContent());
        template.convertAndSend("/topic/messages/"+roomName, messageModel);
    }

    @RequestMapping(value = "/{roomName}", method = GET, produces = APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<MessageModel> fetchNextMessagesForRoom(@PathVariable String roomName, @RequestParam(required = false) UUID fromMessageId, @RequestParam(required = false) int fetchSize) {
        UUID fromLastMessage = fromMessageId == null ? UUIDs.timeBased() : fromMessageId;
        final int pageSize = fetchSize <= 0 ? DEFAULT_MESSAGES_FETCH_SIZE : fetchSize;
        Validator.validateNotBlank(roomName, "Room name can not be blank for posting new message");
        return service.fetchNextMessagesForRoom(roomName, fromLastMessage, pageSize);
    }
}
