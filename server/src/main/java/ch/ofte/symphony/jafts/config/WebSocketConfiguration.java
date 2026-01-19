package ch.ofte.symphony.jafts.config;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {
    @Override
    public void registerWebSocketHandlers(@NotNull WebSocketHandlerRegistry registry) {
        registry.addHandler(new SocketHandler(), "/test-socket-protocol").setAllowedOrigins("*");
    }

    @Component
    private static class SocketHandler extends TextWebSocketHandler {
        List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

        @Override
        protected void handleTextMessage(@NotNull WebSocketSession session, @NotNull TextMessage message) throws Exception {
            for (WebSocketSession webSocketSession : sessions) {
                if (webSocketSession.isOpen() && !session.getId().equals(webSocketSession.getId())) {
                    webSocketSession.sendMessage(message);
                }
            }
        }

        @Override
        public void afterConnectionEstablished(@NotNull  WebSocketSession session) throws Exception {
            sessions.add(session);
        }
    }
}
