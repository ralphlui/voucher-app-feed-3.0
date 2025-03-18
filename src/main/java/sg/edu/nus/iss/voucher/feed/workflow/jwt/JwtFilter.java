package sg.edu.nus.iss.voucher.feed.workflow.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sg.edu.nus.iss.voucher.feed.workflow.dto.*;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.*;

import java.io.IOException;

import io.jsonwebtoken.*;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private JWTService jwtService;

	@Autowired
	private AuditService auditLogService;

	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	private String userID;
	private String apiEndpoint;
	private HTTPVerb httpMethod;
	
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
	        throws ServletException, IOException {
	    
	    String authorizationHeader = request.getHeader("Authorization");

	    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
	        handleException(response, "Authorization header is missing or invalid", HttpServletResponse.SC_UNAUTHORIZED,"");
	        return;
	    }

	    String jwtToken = authorizationHeader.substring(7);

	    try {
	        UserDetails userDetails = jwtService.getUserDetail(jwtToken);
	        if (jwtService.validateToken(jwtToken, userDetails)) {
	            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
	                    userDetails, null, userDetails.getAuthorities());
	            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
	            SecurityContextHolder.getContext().setAuthentication(authentication);
	        } else {
	            handleException(response, "Invalid or expired JWT token", HttpServletResponse.SC_UNAUTHORIZED,jwtToken);
	            return;
	        }
	    } catch (ExpiredJwtException e) {
	        handleException(response, "JWT token is expired", HttpServletResponse.SC_UNAUTHORIZED,jwtToken);
	        return;
	    } catch (MalformedJwtException | SecurityException e) {
	        handleException(response, "Invalid JWT token", HttpServletResponse.SC_UNAUTHORIZED,jwtToken);
	        return;
	    } catch (Exception e) {
	        handleException(response, e.getMessage(), HttpServletResponse.SC_UNAUTHORIZED,jwtToken);
	        return;
	    }

	    filterChain.doFilter(request, response);
	}


	private void handleException(HttpServletResponse response, String message, int status,String token) throws IOException {
		TokenErrorResponse.sendErrorResponse(response, message, status, "UnAuthorized");
		AuditDTO auditDTO = auditLogService.createAuditDTO(userID, "", activityTypePrefix, apiEndpoint, httpMethod);
		auditLogService.logAudit(auditDTO, status, message,token);
	}
	
}

