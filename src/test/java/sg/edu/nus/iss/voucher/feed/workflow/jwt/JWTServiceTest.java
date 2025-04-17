package sg.edu.nus.iss.voucher.feed.workflow.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.PublicKey;
import java.util.Date;

import io.jsonwebtoken.Claims;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;

import sg.edu.nus.iss.voucher.feed.workflow.configuration.JWTConfig;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

@SpringBootTest
@ActiveProfiles("test")
class JWTServiceTest {

    @Mock
    private JWTConfig jwtConfig;

    @Mock
    private JSONReader jsonReader;

    @InjectMocks
    private JWTService jwtService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testValidateToken_validToken_ReturnTrue() throws Exception {
        String email = "test@example.com";
        Claims claims = mock(Claims.class);
        when(claims.get("userEmail", String.class)).thenReturn(email);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 10000)); // not expired

        JWTService spy = spy(jwtService);
        doReturn(claims).when(spy).extractAllClaims(any());

        UserDetails userDetails = User.withUsername(email).password("password").roles("USER").build();
        assertTrue(spy.validateToken("auth-token", userDetails));
    }

    @Test
    public void testIsTokenExpired_expiredToken_ReturnTrue() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() - 10000)); // expired

        JWTService spy = spy(jwtService);
        doReturn(claims).when(spy).extractAllClaims(any());

        assertTrue(spy.isTokenExpired("auth-token"));
    }

    @Test
    public void testGetUserDetail() throws Exception {
        String token = "auth-token";
        String userId = "123";

        sg.edu.nus.iss.voucher.feed.workflow.pojo.User user = new sg.edu.nus.iss.voucher.feed.workflow.pojo.User();
        user.setEmail("test@example.com");
        user.setPassword("password123");
        user.setRole("MERCHANT");

        JWTService spy = spy(jwtService);
        ReflectionTestUtils.setField(spy, "jsonReader", jsonReader);

        doReturn(userId).when(spy).extractUserID(token);
        when(jsonReader.getActiveUserDetails(userId, token)).thenReturn(user);

        UserDetails userDetails = spy.getUserDetail(token);

        assertNotNull(userDetails);
        assertEquals("test@example.com", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
            .anyMatch(granted -> granted.getAuthority().equals("ROLE_MERCHANT")));
    }


    @Test
    public void testRetrieveUserName_ReturnUserName() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.get("userName", String.class)).thenReturn("Test");

        JWTService spy = spy(jwtService);
        doReturn(claims).when(spy).extractAllClaims(any());

        String username = spy.retrieveUserName("dummy-token");
        assertEquals("Test", username);
    }

    @Test
    public void testHashWithSHA256_ReturnValidHash() {
    	String token = "auth-token";
        String hashed = jwtService.hashWithSHA256(token);
        assertNotNull(hashed);
        assertTrue(hashed.length() > 0);
    }

    @Test
    public void testGetUserIdByAuthHeader_ReturnCorrectUserId() throws Exception {
        String token = "Bearer auth-token";
        JWTService spy = spy(jwtService);
        doReturn("user-id-123").when(spy).extractUserID("auth-token");

        String userId = spy.getUserIdByAuthHeader(token);
        assertEquals("user-id-123", userId);
    }
   

    @Test
    public void testExtractAllClaims_ReturnClaims() throws Exception {

        String token = "auth-token";
        Claims mockClaims = mock(Claims.class);

        JWTService spy = spy(jwtService);
        doReturn(mockClaims).when(spy).extractAllClaims(token);

        Claims claims = spy.extractAllClaims(token);

        assertNotNull(claims);
    }
    
 
}
