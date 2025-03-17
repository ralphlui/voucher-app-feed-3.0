package sg.edu.nus.iss.voucher.feed.workflow.jwt;

import java.util.Date;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import sg.edu.nus.iss.voucher.feed.workflow.configuration.JWTConfig;
import sg.edu.nus.iss.voucher.feed.workflow.pojo.User;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class JWTService {

	@Autowired
	private JWTConfig jwtConfig;
	
	@Autowired
	ApplicationContext context;
	
	@Autowired
	JSONReader jsonReader;

	public PublicKey loadPublicKey() throws Exception {
		byte[] keyBytes = Base64.getDecoder().decode(jwtConfig.getJWTPubliceKey());
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
	}

	public String extractUserID(String token) throws JwtException, IllegalArgumentException, Exception {
		// TODO Auto-generated method stub
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimResolver)
			throws JwtException, IllegalArgumentException, Exception {
		final Claims cliams = extractAllClaims(token);
		return claimResolver.apply(cliams);
	}

	public Claims extractAllClaims(String token) throws JwtException, IllegalArgumentException, Exception {
		return Jwts.parser().verifyWith(loadPublicKey()).build().parseSignedClaims(token).getPayload();
	}
	
	public UserDetails getUserDetail(String token) throws JwtException, IllegalArgumentException, Exception {
		String userID = extractUserID(token);
		User user = jsonReader.getActiveUserDetails(userID,token);
		UserDetails userDetails = org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail()).password(user.getPassword()).roles(user.getRole().toString())
				.build();
		return userDetails;
	}

	public Boolean validateToken(String token, UserDetails userDetails)
			throws JwtException, IllegalArgumentException, Exception {
		Claims claims = extractAllClaims(token);
		String userEmail = claims.get("userEmail", String.class);
		return (userEmail.equals(userDetails.getUsername()) && !isTokenExpired(token));
	}

	public boolean isTokenExpired(String token) throws JwtException, IllegalArgumentException, Exception {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token) throws JwtException, IllegalArgumentException, Exception {
		return extractClaim(token, Claims::getExpiration);
	}
	
    public String hashWithSHA256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing refresh token", e);
        }
    }
    
    public String getUserIdByAuthHeader(String authHeader) throws JwtException, IllegalArgumentException, Exception {
    	String userID ="";
    	String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
    	if(jwtToken != null) {
    		 userID = extractUserID(jwtToken);
    		/*UserDetails userDetails  = getUserDetail(jwtToken);
    		if(userDetails != null) {
    		boolean validToken = validateToken(jwtToken, userDetails);
    		if(validToken) {
    			 userID = extractUserID(jwtToken);
    		}
    		}*/
    	}
		return userID;
    }

}
