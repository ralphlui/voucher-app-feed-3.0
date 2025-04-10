package sg.edu.nus.iss.voucher.feed.workflow.jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
 
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
 
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext; 
import sg.edu.nus.iss.voucher.feed.workflow.configuration.JWTConfig;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

@ExtendWith(MockitoExtension.class)
class JWTServiceTest {

    @InjectMocks
    private JWTService jwtService;

    @Mock
    private JWTConfig jwtConfig;

    @Mock
    private JSONReader jsonReader;

    @Mock
    private ApplicationContext context;

    private PublicKey publicKey;
    private String testToken;

    @BeforeEach
    void setUp() throws Exception {
        
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        publicKey = keyPair.getPublic();
 
        when(jwtConfig.getJWTPubliceKey()).thenReturn(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
 
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "testUserID");
        claims.put("userEmail", "test@example.com");
        claims.put("userName", "Test User");

        testToken = Jwts.builder()
                .claims(claims)
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60)) // Expires in 1 hour
                .signWith(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256)) // Temporary key
                .compact();
    }

    @Test
    void testLoadPublicKey() throws Exception {
        PublicKey loadedKey = jwtService.loadPublicKey();
        assertNotNull(loadedKey);
        assertArrayEquals(publicKey.getEncoded(), loadedKey.getEncoded());
    }

}
