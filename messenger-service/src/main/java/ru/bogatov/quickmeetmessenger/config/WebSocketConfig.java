package ru.bogatov.quickmeetmessenger.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;


import static ru.bogatov.quickmeetmessenger.constant.RouteConstant.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${spring.rabbitmq.host}")
    String host;

    @Value("${spring.rabbitmq.username}")
    String username;

    @Value("${spring.rabbitmq.password}")
    String password;

    private final JwtProvider provider;

    public WebSocketConfig(JwtProvider provider) {
        this.provider = provider;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(REGISTRY)
                .setAllowedOrigins("*")
                .withSockJS();
        registry.addEndpoint(REGISTRY)
                .setAllowedOrigins("*");
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        //config.enableSimpleBroker(TOPIC_DESTINATION_PREFIX);
        config.enableStompBrokerRelay(TOPIC_DESTINATION_PREFIX)
                .setAutoStartup(true)
                .setRelayHost(host)
                .setRelayPort(61613)
                //.setSystemHeartbeatSendInterval(0)
                .setClientLogin(username)
                .setClientPasscode(password)
                .setSystemLogin(username)
                .setSystemPasscode(password);
        config.setApplicationDestinationPrefixes(TOPIC_DESTINATION_PREFIX);
        //config.setUserDestinationPrefix(TOPIC_DESTINATION_PREFIX);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor());
        WebSocketMessageBrokerConfigurer.super.configureClientInboundChannel(registration);
    }

    @Bean
    public WebSocketAuthInterceptor authInterceptor() {
        return new WebSocketAuthInterceptor(this.provider);
    }
}
