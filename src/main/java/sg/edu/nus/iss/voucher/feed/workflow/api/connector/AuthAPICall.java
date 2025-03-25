package sg.edu.nus.iss.voucher.feed.workflow.api.connector;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
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
	
	
	public String getActiveUser(String userId,String authorizationHeader ) {
	    String responseStr = "";
	    
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    try {
	    	String url = authURL.trim() + "/active";
	        logger.info("getSpeicficActiveUsers url : " + url);
	       
	        RequestConfig config = RequestConfig.custom()
	                .setConnectTimeout(30000)
	                .setConnectionRequestTimeout(30000)
	                .setSocketTimeout(30000)
	                .build();
	        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	        HttpPost request = new HttpPost(url);
	        request.setHeader("Authorization", authorizationHeader);
	        request.setHeader("Content-Type", "application/json");

	        String jsonBody = "{\"userId\": \"" + userId + "\"}";
	        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));

	        CloseableHttpResponse httpResponse = httpClient.execute(request);
	        try {
	            byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());
	            responseStr = new String(responseByteArray, Charset.forName("UTF-8"));
	            logger.info("getSpeicficActiveUsers: " + responseStr);
	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.error("getSpeicficActiveUsers exception... {}", e.toString());
	        } finally {
	            try {
	                httpResponse.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	                logger.error("getSpeicficActiveUsers exception... {}", e.toString());
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        logger.error("getSpeicficActiveUsers exception... {}", ex.toString());
	    }
	    return responseStr;
	}
	

	
	public String getAllActiveUsers(String authorizationHeader,int page, int size) {
	    String responseStr = "";
	   
	    
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    try {
	        String url = authURL.trim()  + "?page=" + page + "&size=" + size;
	        logger.info("getAllActiveUsers url : " + url);
	        RequestConfig config = RequestConfig.custom()
	                .setConnectTimeout(30000)
	                .setConnectionRequestTimeout(30000)
	                .setSocketTimeout(30000)
	                .build();
	        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	        HttpGet request = new HttpGet(url);
	        request.setHeader("Authorization", authorizationHeader);
	        CloseableHttpResponse httpResponse = httpClient.execute(request);
	        try {
	            byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());
	            responseStr = new String(responseByteArray, Charset.forName("UTF-8"));
	            logger.info("getAllActiveUsers: " + responseStr);
	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.error("getAllActiveUsers exception... {}", e.toString());
	        } finally {
	            try {
	                httpResponse.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	                logger.error("getAllActiveUsers exception... {}", e.toString());
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        logger.error("getAllActiveUsers exception... {}", ex.toString());
	    }
	    return responseStr;
	}
	
	public String getAccessToken(String email ) {
	    String responseStr = "";
	    
	    CloseableHttpClient httpClient = HttpClients.createDefault();
	    try {
	    	 
	        String url = authURL.trim() + "/accessToken";
	        logger.info("getAccessToken url : " + url);
	        RequestConfig config = RequestConfig.custom()
	                .setConnectTimeout(30000)
	                .setConnectionRequestTimeout(30000)
	                .setSocketTimeout(30000)
	                .build();
	        httpClient = HttpClientBuilder.create().setDefaultRequestConfig(config).build();
	        HttpPost request = new HttpPost(url);
	        request.setHeader("Content-Type", "application/json");
	        
	        StringEntity entity = new StringEntity("{\"email\":\"" + email + "\"}", Charset.forName("UTF-8"));
	        request.setEntity(entity);

	        CloseableHttpResponse httpResponse = httpClient.execute(request);
	        try {
	            byte[] responseByteArray = EntityUtils.toByteArray(httpResponse.getEntity());
	            responseStr = new String(responseByteArray, Charset.forName("UTF-8"));
	            logger.info("getAccessToken: " + responseStr);
	        } catch (Exception e) {
	            e.printStackTrace();
	            logger.error("getAccessToken exception... {}", e.toString());
	        } finally {
	            try {
	                httpResponse.close();
	            } catch (IOException e) {
	                e.printStackTrace();
	                logger.error("getAccessToken exception... {}", e.toString());
	            }
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        logger.error("getAccessToken exception... {}", ex.toString());
	    }
	    return responseStr;
	}
	
}
