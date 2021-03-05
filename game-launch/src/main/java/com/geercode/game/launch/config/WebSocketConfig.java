package com.geercode.game.launch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class WebSocketConfig {
    @Bean
    public CustomWebSocketHandler customWebSocketHandler() {
        return new CustomWebSocketHandler();
    }

    @Bean
    public HandlerMapping webSocketMapping(CustomWebSocketHandler webSocketHandler) {
        final Map<String, WebSocketHandler> map = new HashMap<>(1);
        map.put("/echo", webSocketHandler);

        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    public static class CustomWebSocketHandler implements WebSocketHandler {
        private static final ConcurrentHashMap<String, WebSocketSession> sessionHolder = new ConcurrentHashMap<>();

        public static WebSocketSession getSession(String sessionKey) {
            return sessionHolder.get(sessionKey);
        }

        @Override
        public Mono<Void> handle(WebSocketSession session) {
            /*return session.send(
                    session.receive().map(
                            msg -> session.textMessage("ECHO -> " + msg.getPayloadAsText())));*/
            sessionHolder.put("1", session);
            Mono<Void> input = session.receive().doOnNext(s -> System.out.println("123")).then();
            Mono<Void> output = session.send(Flux.interval(Duration.ofSeconds(1)).map(c -> session.textMessage(c.toString())));
            //Mono<Void> output = session.send(Mono.just(session.textMessage("你好")));
            return Mono.zip(input, output).then();
        }
    }
}
