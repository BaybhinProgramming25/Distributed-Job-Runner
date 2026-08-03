package com.example.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.util.List;

import com.example.service.JwtService;


@Configuration
@EnableWebSocketMessageBroker
public class WebSocketAuthConfig implements WebSocketMessageBrokerConfigurer{

    private final JwtService jwtService;

    public WebSocketAuthConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {
            
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                var accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    String token = accessor.getFirstNativeHeader("Authorization");
                    if (token == null || !token.startsWith("Bearer ")) {
                        throw new MessagingException("Missing Token");
                    }
                    String username = jwtService.verifyAndGetUsername(token.substring(7));
                    accessor.setUser(new UsernamePasswordAuthenticationToken(username, null, List.of()));
                }
                return message;
            }
        });
    }

    
}
