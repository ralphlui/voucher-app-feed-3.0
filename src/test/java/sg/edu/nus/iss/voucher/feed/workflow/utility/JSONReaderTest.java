package sg.edu.nus.iss.voucher.feed.workflow.utility;


import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.pojo.User;

import java.util.ArrayList;

class JSONReaderTest {

    @Mock
    private AuthAPICall apiCall;

    @InjectMocks
    private JSONReader jsonReader;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void readFeedMessage_shouldReturnMessagePayload_whenValidMessage() throws ParseException {
    
        String message = "{\"email\":\"test@example.com\", \"campaign\":{\"campaignId\":\"123\", \"description\":\"Campaign 1\"}, \"store\":{\"storeId\":\"456\", \"name\":\"Store A\"}}";
        MessagePayload expected = new MessagePayload();
        expected.setEmail("test@example.com");
        expected.setCampaignId("123");
        expected.setCampaignDescription("Campaign 1");
        expected.setStoreId("456");
        expected.setStoreName("Store A");
 
        MessagePayload result = jsonReader.readFeedMessage(message);
 
        assertEquals(expected.getEmail(), result.getEmail());
        assertEquals(expected.getCampaignId(), result.getCampaignId());
        assertEquals(expected.getCampaignDescription(), result.getCampaignDescription());
        assertEquals(expected.getStoreId(), result.getStoreId());
        assertEquals(expected.getStoreName(), result.getStoreName());
    }

  
    @Test
    void getActiveUser_shouldReturnUsername_whenValidResponse() throws Exception {
     
        String userId = "1";
        String token = "valid_token";
        String apiResponse = "{\"data\": {\"username\": \"User1\"}}";
        when(apiCall.getActiveUser(userId, token)).thenReturn(apiResponse);
 
        String userName = jsonReader.getActiveUser(userId, token);
 
        assertEquals("User1", userName);
    }

    @Test
    void getAccessToken_shouldReturnToken_whenValidResponse() throws Exception {
   
        String email = "user@example.com";
        String apiResponse = "{\"data\": {\"token\": \"valid-token\"}}";
        when(apiCall.getAccessToken(email)).thenReturn(apiResponse);
 
        String token = jsonReader.getAccessToken(email);
 
        assertEquals("valid-token", token);
    }

    @Test
    void getActiveUserDetails_shouldReturnUserDetails_whenValidResponse() throws Exception {
       
        String userId = "1";
        String token = "valid_token";
        String apiResponse = "{\"data\": {\"username\": \"User1\", \"email\": \"user1@example.com\", \"role\": \"admin\"}}";
        when(apiCall.getActiveUser(userId, token)).thenReturn(apiResponse);
 
        User user = jsonReader.getActiveUserDetails(userId, token);
 
        assertEquals("User1", user.getUsername());
        assertEquals("user1@example.com", user.getEmail());
        assertEquals("admin", user.getRole());
    }

    @Test
    void getMessageFromResponse_shouldReturnMessage_whenValidResponse() {
  
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("message", "Success");
 
        String message = jsonReader.getMessageFromResponse(jsonResponse);
 
        assertEquals("Success", message);
    }

    @Test
    void getSuccessFromResponse_shouldReturnSuccessFlag_whenValidResponse() {
       
        JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
 
        Boolean success = jsonReader.getSuccessFromResponse(jsonResponse);
 
        assertTrue(success);
    }

    
    
    @Test
    void testGetAllActiveUsers_withValidPageMaxSize() {
       
        String token = "valid_token";
        String apiResponse = "{"
                + "\"totalRecord\": 3,"
                + "\"data\": ["
                + "{ \"userID\": \"1\", \"email\": \"user1@example.com\", \"username\": \"user1\" },"
                + "{ \"userID\": \"2\", \"email\": \"user2@example.com\", \"username\": \"user2\" },"
                + "{ \"userID\": \"3\", \"email\": \"user3@example.com\", \"username\": \"user3\" }"
                + "]"
                + "}";

        when(apiCall.getAllActiveUsers(eq(token), anyInt(), anyInt())).thenReturn(apiResponse);
 
        jsonReader.pageMaxSize = "2";
 
        ArrayList<User> users = jsonReader.getAllActiveUsers(token);
 
        assertNotNull(users);
        assertEquals(6, users.size());
        assertEquals("user1@example.com", users.get(0).getEmail());
        assertEquals("user2@example.com", users.get(1).getEmail());
        assertEquals("user3@example.com", users.get(2).getEmail());
 
        verify(apiCall, times(2)).getAllActiveUsers(eq(token), anyInt(), anyInt());
    }

   

}
