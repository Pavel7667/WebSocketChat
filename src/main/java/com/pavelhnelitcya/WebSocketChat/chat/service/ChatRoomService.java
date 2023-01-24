package com.pavelhnelitcya.WebSocketChat.chat.service;

import org.redisson.api.RListReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * In case User come to ChatRoom URL and set correct "room" name
 * give access into "room"
 */
@Service
public class ChatRoomService implements WebSocketHandler {

    @Autowired
    private RedissonReactiveClient client;

    /**
     * Show and push messages into chosen ChatRoom
     */
    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        // User input text with "room" title
        String room = getChatRoomName(webSocketSession);
        RTopicReactive topic = this.client.getTopic(room, StringCodec.INSTANCE);
        RListReactive<String> list = this.client.getList("history:" + room, StringCodec.INSTANCE);
        // subscribe - take request text "Message" from user
        webSocketSession.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .flatMap(msg -> list.add(msg).then(topic.publish(msg)))
                .subscribe();

        // publisher - push "Message" as response into chat
        Flux<WebSocketMessage> flux = topic.getMessages(String.class)
                .startWith(list.iterator())
                .map(webSocketSession::textMessage);
        return webSocketSession.send(flux);
    }

    /**
     * In case correct name of Chat name -> give access to this Room
     */
    private String getChatRoomName(WebSocketSession socketSession) {
        URI uri = socketSession.getHandshakeInfo().getUri();
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .toSingleValueMap()
                .getOrDefault("room", "default");
    }
}

