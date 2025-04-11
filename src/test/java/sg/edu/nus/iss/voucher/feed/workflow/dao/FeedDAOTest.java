package sg.edu.nus.iss.voucher.feed.workflow.dao;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.internal.IteratorSupport;
import com.amazonaws.services.dynamodbv2.document.spec.ScanSpec;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.model.*;

import sg.edu.nus.iss.voucher.feed.workflow.entity.Feed;

@SpringBootTest
@ActiveProfiles("test")
class FeedDAOTest {

	@Mock
	private AmazonDynamoDB amazonDynamoDB;

	@Mock
	private DynamoDB dynamoDB;

	@Mock
	private Table mockTable;
	
	@Mock
    private UpdateItemOutcome mockOutcome;


	@InjectMocks
	private FeedDAO feedDAO;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		feedDAO = new FeedDAO(amazonDynamoDB);
		ReflectionTestUtils.setField(feedDAO, "feedTbl", "FeedTable");
		ReflectionTestUtils.setField(feedDAO, "dynamoDB", dynamoDB);
	}

	@Test
	void testSaveFeed() {
		Feed inputFeed = new Feed();
		inputFeed.setUserId("user123");
		inputFeed.setUserName("Alice");
		inputFeed.setEmail("alice@example.com");
		inputFeed.setCampaignId("camp456");
		inputFeed.setCampaignDescription("Campaign Desc");
		inputFeed.setStoreId("store789");
		inputFeed.setStoreName("Store Name");

		when(dynamoDB.getTable("FeedTable")).thenReturn(mockTable);
		when(mockTable.putItem(any(Item.class))).thenReturn(mock(PutItemOutcome.class));
		when(amazonDynamoDB.getItem(any(GetItemRequest.class)))
				.thenReturn(new GetItemResult().withItem(new HashMap<>() {
					{
						put("FeedId", new AttributeValue("feed123"));
						put("UserId", new AttributeValue("user123"));
						put("UserName", new AttributeValue("Alice"));
						put("Email", new AttributeValue("alice@example.com"));
						put("CampaignId", new AttributeValue("camp456"));
						put("CampaignDescription", new AttributeValue("Campaign Desc"));
						put("StoreId", new AttributeValue("store789"));
						put("StoreName", new AttributeValue("Store Name"));
						put("IsDeleted", new AttributeValue("0"));
						put("IsReaded", new AttributeValue("0"));
						put("ReadTime", new AttributeValue(""));
						put("CreatedDate", new AttributeValue(LocalDateTime.now().toString()));
					}
				}));

		Feed result = feedDAO.saveFeed(inputFeed);

		assertNotNull(result);
		assertEquals("user123", result.getUserId());
		assertEquals("Alice", result.getUserName());
	}

	@Test
	void testFindById() {
		String feedId = "feed123";
		when(amazonDynamoDB.getItem(any(GetItemRequest.class)))
				.thenReturn(new GetItemResult().withItem(Map.of("FeedId", new AttributeValue(feedId), "UserId",
						new AttributeValue("user123"), "Email", new AttributeValue("test@example.com"))));

		Feed result = feedDAO.findById(feedId);
		assertNotNull(result);
		assertEquals(feedId, result.getFeedId());
		assertEquals("user123", result.getUserId());
	}

	@Test
	void testCheckFeedExistsByUserAndCampaign_false() {
		ItemCollection<ScanOutcome> mockItems = mock(ItemCollection.class);
		IteratorSupport<Item, ScanOutcome> mockIterator = mock(IteratorSupport.class);

		when(dynamoDB.getTable("FeedTable")).thenReturn(mockTable);
		when(mockTable.scan(any(ScanSpec.class))).thenReturn(mockItems);
		when(mockItems.iterator()).thenReturn(mockIterator);
		when(mockIterator.hasNext()).thenReturn(false);

		boolean exists = feedDAO.checkFeedExistsByUserAndCampaign("user123", "camp456");

		assertFalse(exists);
	}

	@Test
	void testGetAllFeedByUserId() {
		String userId = "user123";
		int page = 0;
		int size = 10;

		ItemCollection<ScanOutcome> mockItems = mock(ItemCollection.class);
		IteratorSupport<Item, ScanOutcome> mockIterator = mock(IteratorSupport.class);

		Item mockItem = new Item().withString("FeedId", "feed123").withString("CampaignId", "camp456")
				.withString("CampaignDescription", "Campaign Desc").withString("StoreId", "store789")
				.withString("StoreName", "Store Name").withString("UserId", "user123")
				.withString("Email", "alice@example.com").withString("UserName", "Alice").withString("IsDeleted", "0")
				.withString("IsReaded", "0").withString("ReadTime", "")
				.withString("CreatedDate", LocalDateTime.now().toString());

		when(dynamoDB.getTable("FeedTable")).thenReturn(mockTable);
		when(mockTable.scan(any(ScanSpec.class))).thenReturn(mockItems);
		when(mockItems.iterator()).thenReturn(mockIterator);
		when(mockIterator.hasNext()).thenReturn(true, false);
		when(mockIterator.next()).thenReturn(mockItem);

		List<Feed> result = feedDAO.getAllFeedByUserId(userId, page, size);

		assertNotNull(result);
		assertEquals(1, result.size());
		Feed feed = result.get(0);
		assertEquals("feed123", feed.getFeedId());
		assertEquals("user123", feed.getUserId());
		assertEquals("alice@example.com", feed.getEmail());
		assertEquals("Alice", feed.getUserName());
	}

	@Test
	void testUpdateReadStatus_success() {
		String feedId = "feed123";

		// Mock the DynamoDB table and outcome
		when(dynamoDB.getTable("FeedTable")).thenReturn(mockTable);
		when(mockTable.updateItem(any(UpdateItemSpec.class))).thenReturn(mockOutcome);
		when(mockOutcome.getItem()).thenReturn(new Item());

		// Act
		boolean result = feedDAO.upateReadStatus(feedId);

		// Assert
		assertTrue(result);
		verify(mockTable, times(1)).updateItem(any(UpdateItemSpec.class));
	}

	@Test
	void testUpdateReadStatus_failure() {
		String feedId = "feed123";

		// Mock the DynamoDB table and outcome
		when(dynamoDB.getTable("FeedTable")).thenReturn(mockTable);
		when(mockTable.updateItem(any(UpdateItemSpec.class))).thenReturn(null);

		// Act
		boolean result = feedDAO.upateReadStatus(feedId);

		// Assert
		assertFalse(result);
		verify(mockTable, times(1)).updateItem(any(UpdateItemSpec.class));
	}

	@Test
	void testUpdateReadStatus_exception() {
		String feedId = "feed123";

		// Simulate an exception when calling updateItem
		when(dynamoDB.getTable("FeedTable")).thenReturn(mockTable);
		when(mockTable.updateItem(any(UpdateItemSpec.class))).thenThrow(new RuntimeException("DynamoDB error"));

		// Act
		boolean result = feedDAO.upateReadStatus(feedId);

		// Assert
		assertFalse(result);
	}
}
