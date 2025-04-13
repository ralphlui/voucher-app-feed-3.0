package sg.edu.nus.iss.voucher.feed.workflow.api.connector;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class AuthAPICall {

	@Value("${auth.api.url}")
    private String authURL;

	private static final Logger logger = LoggerFactory.getLogger(AuthAPICall.class);
	private static final String GET_SPECIFIC_ACTIVE_USERS_EXCEPTION_MSG = "getSpecificActiveUsers exception... {}";

	
	private static final String UTF_8 = "UTF-8";
	
	RequestConfig config = RequestConfig.custom()
            .setConnectTimeout(30000)
            .setConnectionRequestTimeout(30000)
            .setSocketTimeout(30000)
            .build();

	
	public String getActiveUser(String userId,String authorizationHeader ) {
	    String responseStr = "";
	    
	    try (CloseableHttpClient httpClient = HttpClientBuilder.create()
	            .setDefaultRequestConfig(config)
	            .build()) {
	    	String url = authURL.trim() + "/active";
	        logger.info("getSpeicficActiveUsers url : " + url);
	       
	        HttpPost request = new HttpPost(url);
	        request.setHeader("Authorization", authorizationHeader);
	        request.setHeader("Content-Type", "application/json");

	        String jsonBody = "{\"userId\": \"" + userId + "\"}";
	        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

	        CloseableHttpResponse httpResponse = httpClient.execute(request);
	        try {
	            byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());

	            responseStr = new String(responseByteArray, Charset.forName(UTF_8));
	            logger.info("getSpeicficActiveUsers: {}", responseStr);

	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.error(GET_SPECIFIC_ACTIVE_USERS_EXCEPTION_MSG, e.toString());
	        } finally {
	            try {
	                httpResponse.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	                logger.error(GET_SPECIFIC_ACTIVE_USERS_EXCEPTION_MSG, e.toString());
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        logger.error(GET_SPECIFIC_ACTIVE_USERS_EXCEPTION_MSG, ex.toString());
	    }
	    return responseStr;
	}
	

	
	public String getAllActiveUsers(String authorizationHeader, int page, int size) {
	    String url = authURL.trim() + "?page=" + page + "&size=" + size;
	    logger.info("getAllActiveUsers url : {}", url);


	    String responseStr = "";


	    try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	         CloseableHttpResponse httpResponse = httpClient.execute(createHttpGet(url, authorizationHeader))) {

	        byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());
	        responseStr = new String(responseByteArray, Charset.forName(UTF_8));
	        logger.info("getAllActiveUsers response: {}", responseStr);

	    } catch (IOException e) {
	        logger.error("getAllActiveUsers exception: {}", e.toString(), e);

	    }

	    return responseStr;
	}

	private HttpGet createHttpGet(String url, String authorizationHeader) {
	    HttpGet request = new HttpGet(url);
	    request.setHeader("Authorization", authorizationHeader);
	    return request;
	}
	
	public String getAccessToken(String email) {
	    String responseStr = "";
	    String url = authURL.trim() + "/accessToken";
	    logger.info("getAccessToken url: {}", url);


	    try (CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build()) {
	        HttpPost request = new HttpPost(url);
	        request.setHeader("Content-Type", "application/json");

	        String jsonPayload = String.format("{\"email\":\"%s\"}", email);
	        StringEntity entity = new StringEntity(jsonPayload, Charset.forName(UTF_8));
	        request.setEntity(entity);

	        try (CloseableHttpResponse httpResponse = httpClient.execute(request)) {
	            byte[] responseBytes = EntityUtils.toByteArray(httpResponse.getEntity());
	            responseStr = new String(responseBytes, Charset.forName(UTF_8));
	            logger.info("getAccessToken response: {}", responseStr);
	        }

	    } catch (Exception e) {
	        logger.error("getAccessToken exception: {}", e.toString(), e);
	    }

	    return responseStr;
	}
	
}
