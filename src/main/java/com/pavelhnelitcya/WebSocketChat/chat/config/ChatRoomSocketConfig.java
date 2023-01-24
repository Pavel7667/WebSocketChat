package com.pavelhnelitcya.WebSocketChat.chat.config;


import com.pavelhnelitcya.WebSocketChat.chat.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;

import java.util.Map;

/**
 * If User call this URL, send "Bean" with this method
 */
@Configuration
public class ChatRoomSocketConfig {

    @Autowired
    private ChatRoomService chatRoomService;

    @Bean
    public HandlerMapping handlerMapping(){
        Map<String, WebSocketHandler> map = Map.of(
                "/chat", chatRoomService
        );
        // add "-1" to give hyperPriority for this method compared to other controllers
        return new SimpleUrlHandlerMapping(map, -1);
    }

}