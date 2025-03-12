package sg.edu.nus.iss.voucher.feed.workflow.configuration;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import sg.edu.nus.iss.voucher.feed.workflow.jwt.JWTService;

public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JWTService jwtService;

    public JwtHandshakeInterceptor(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {

        String jwtToken = extractTokenFromAuthorizationHeader(request);

        if (jwtToken == null || jwtToken.isEmpty()) {
            System.out.println("WebSocket connection attempted without an Authorization token.");
            return false;
        }

        try {
            
            UserDetails userDetails = jwtService.getUserDetail(jwtToken);
            if (jwtService.validateToken(jwtToken, userDetails)) {
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                 
                SecurityContextHolder.getContext().setAuthentication(authentication);
                attributes.put("user", userDetails); // Store user in WebSocket session
                return true;  
            }
        } catch (ExpiredJwtException e) {
            System.out.println("JWT Token Expired");
        } catch (MalformedJwtException | SecurityException e) {
            System.out.println("Malformed JWT Token");
        } catch (Exception e) {
            System.out.println("Unknown error during JWT validation: " + e.getMessage());
        }
        return false; 
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, 
                               WebSocketHandler wsHandler, Exception exception) {
        // No need to implement anything here
    }

    private String extractTokenFromAuthorizationHeader(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String authHeader = headers.getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);  
        }

        return null;  
    }
}
