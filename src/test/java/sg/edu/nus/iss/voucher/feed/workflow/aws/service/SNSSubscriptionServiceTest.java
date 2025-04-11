package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;
import sg.edu.nus.iss.voucher.feed.workflow.entity.MessagePayload;
import sg.edu.nus.iss.voucher.feed.workflow.pojo.User;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@SpringBootTest
@ActiveProfiles("test")
public class SNSSubscriptionServiceTest {

    @InjectMocks
    private SNSSubscriptionService snsSubscriptionService;

    @Mock
    private JSONReader jsonReader;

    @Mock
    private FeedDAO feedDAO;

    @Mock
    private EmailStrategy emailStrategy;

    private MessagePayload samplePayload;
    private User sampleUser;
    private Feed savedFeed;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        samplePayload = new MessagePayload();
        samplePayload.setCampaignId("C123");
        samplePayload.setCampaignDescription("Campaign Desc");
        samplePayload.setStoreId("S123");
        samplePayload.setStoreName("Store Name");
        samplePayload.setEmail("merchant@example.com");

        sampleUser = new User();
        sampleUser.setUserId("user1");
        sampleUser.setEmail("user1@example.com");
        sampleUser.setUsername("User One");

        savedFeed = new Feed();
        savedFeed.setFeedId("F123");
        savedFeed.setUserId(sampleUser.getUserId());
    }

    @Test
    void testProcessNotification_successfulFlow() throws Exception {
        String snsMessage = new ObjectMapper().createObjectNode()
                .put("Message", new ObjectMapper().writeValueAsString(samplePayload))
                .toString();

        String token = "mocked-token";
        ArrayList<User> users = new ArrayList<>(List.of(sampleUser));

        when(jsonReader.readFeedMessage(anyString())).thenReturn(samplePayload);
        when(jsonReader.getAccessToken(anyString())).thenReturn(token);
        when(jsonReader.getAllActiveUsers(eq(token))).thenReturn(users);
        when(feedDAO.checkFeedExistsByUserAndCampaign(any(), any())).thenReturn(false);
        when(feedDAO.saveFeed(any(Feed.class))).thenReturn(savedFeed);
        when(emailStrategy.sendNotification(any(FeedDTO.class))).thenReturn(true);

        String result = snsSubscriptionService.processNotification(snsMessage);

        assertTrue(result.contains("Processed user:user1:true"));
        verify(feedDAO).saveFeed(any(Feed.class));
        verify(emailStrategy).sendNotification(any(FeedDTO.class));
    }

    @Test
    void testProcessNotification_invalidMessage() {
        String snsMessage = "{\"Message\":\"\"}";

        String result = snsSubscriptionService.processNotification(snsMessage);

        assertEquals("Bad Request:Message is null or empty.", result);
    }

    @Test
    void testProcessNotification_nullFeedMsg() throws Exception {
        String payloadJson = "{\"Message\":\"text\"}";

        when(jsonReader.readFeedMessage("text")).thenReturn(null);

        String result = snsSubscriptionService.processNotification(payloadJson);

        assertEquals("Bad Request:Failed to parse the feed message. The message payload is null or invalid.", result);
    }

    @Test
    void testProcessNotification_emptyEmail() throws Exception {
        samplePayload.setEmail("");

        String payloadJson = new ObjectMapper().createObjectNode()
                .put("Message", new ObjectMapper().writeValueAsString(samplePayload))
                .toString();

        when(jsonReader.readFeedMessage(anyString())).thenReturn(samplePayload);

        String result = snsSubscriptionService.processNotification(payloadJson);

        assertEquals("Bad Request:Promoted User is empty.", result);
    }
}
