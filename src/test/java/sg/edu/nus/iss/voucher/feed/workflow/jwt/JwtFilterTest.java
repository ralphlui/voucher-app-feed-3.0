package sg.edu.nus.iss.voucher.feed.workflow.jwt;

import static org.mockito.Mockito.*;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService; 

class JwtFilterTest {

    @InjectMocks
    private JwtFilter jwtFilter;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuditService auditService;

    @Mock
    private FilterChain filterChain;

    @Mock
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jwtFilter.activityTypePrefix = "FEED";
    }

    @Test
    void testMissingAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assert(response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testInvalidAuthorizationHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "InvalidTokenFormat");
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilterInternal(request, response, filterChain);

        assert(response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testValidToken() throws Exception {
        String token = "valid.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getUserDetail(token)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assert(response.getStatus() == 200); // default for successful response
    }

    @Test
    void testExpiredToken() throws Exception {
        String token = "expired.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getUserDetail(token)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assert(response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void testInvalidToken() throws Exception {
        String token = "invalid.jwt.token";

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.getUserDetail(token)).thenThrow(new MalformedJwtException("Malformed"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        assert(response.getStatus() == HttpServletResponse.SC_UNAUTHORIZED);
    }

}
