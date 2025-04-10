package sg.edu.nus.iss.voucher.feed.workflow.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.*;
import com.amazonaws.services.dynamodbv2.model.*;

import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

@ExtendWith(MockitoExtension.class)
class FeedDAOTest {
    
    @Mock
    private AmazonDynamoDB dynamoDBClient;
    
    @Mock
    private Table mockTable;
    
    @Mock
    private ItemCollection<ScanOutcome> mockItemCollection;
    
    @InjectMocks
    private FeedDAO feedDAO;
    
    @BeforeEach
    void setUp() {
        feedDAO = new FeedDAO(dynamoDBClient);
    }
   
    @Test
    void testFindById() {
        Map<String, AttributeValue> mockItem = new HashMap<>();
        mockItem.put("FeedId", new AttributeValue("123"));
        when(dynamoDBClient.getItem(any(GetItemRequest.class))).thenReturn(new GetItemResult().withItem(mockItem));
        
        Feed feed = feedDAO.findById("123");
        assertNotNull(feed);
        assertEquals("123", feed.getFeedId());
    }
    
   
}
