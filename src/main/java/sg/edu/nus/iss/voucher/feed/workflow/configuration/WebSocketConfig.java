package sg.edu.nus.iss.voucher.feed.workflow.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import sg.edu.nus.iss.voucher.feed.workflow.jwt.JWTService;
import sg.edu.nus.iss.voucher.feed.workflow.websocket.handler.NotificationWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final NotificationWebSocketHandler webSocketHandler;

    @Autowired
    private JWTService jwtService;

    public WebSocketConfig(NotificationWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/ws/liveFeeds")
            .setAllowedOrigins("*")
            .addInterceptors(new JwtHandshakeInterceptor(jwtService)); // Register the interceptor
    }
}
