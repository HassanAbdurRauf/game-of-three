package com.hrauf.got.config;

import com.hrauf.got.repository.PlayerRepository;
import com.hrauf.got.service.PlayerService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static java.util.Optional.ofNullable;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String USERNAME_HEADER = "username";

    private final PlayerService playerService;

    public WebSocketConfig(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/game-of-three").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/mq");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setPreservePublishOrder(true);
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new AuthenticatedPlayerChannelInterceptor(playerService));
    }


    static class AuthenticatedPlayerChannelInterceptor implements ChannelInterceptor {

        private final PlayerService playerService;

        public AuthenticatedPlayerChannelInterceptor(PlayerService playerService) {
            this.playerService = playerService;
        }


        @Override
        public Message<?> preSend(Message<?> message, MessageChannel channel) {
            StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                ofNullable(accessor.getFirstNativeHeader(USERNAME_HEADER))
                        .filter(username -> !StringUtils.isEmpty(username))
                        .ifPresentOrElse(username -> {
                            checkPlayerDoesNotExist(username);
                            accessor.setUser(() -> username);
                        }, () -> throwMessagingException("username is required to establish a connection"));
            }

            return message;
        }

        private void checkPlayerDoesNotExist(String username) {
            if (playerService.exists(username)) {
                throwMessagingException("Player with username already connected!!");
            }
        }

        private void throwMessagingException(String errorMessage) {
            throw new MessagingException(errorMessage);
        }
    }
}
