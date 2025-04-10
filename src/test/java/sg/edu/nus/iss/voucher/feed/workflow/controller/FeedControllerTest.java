package sg.edu.nus.iss.voucher.feed.workflow.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.feed.workflow.api.connector.AuthAPICall;
import sg.edu.nus.iss.voucher.feed.workflow.dto.*;
import sg.edu.nus.iss.voucher.feed.workflow.entity.HTTPVerb;
import sg.edu.nus.iss.voucher.feed.workflow.jwt.JWTService;
import sg.edu.nus.iss.voucher.feed.workflow.pojo.User;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;
import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;
import sg.edu.nus.iss.voucher.feed.workflow.utility.JSONReader;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FeedControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private FeedService feedService;

	@MockBean
	private AuthAPICall authAPICall;

	@MockBean
	private AuditService auditService;

	@MockBean
	private JWTService jwtService;

	@MockBean
	private JSONReader jsonReader;

	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	private static FeedDTO feedDTO;
	private static AuditDTO auditDTO;
	static String userId = "user123";
	static String authorizationHeader = "Bearer mock.jwt.token";

	private static FeedRequest apiRequest = new FeedRequest("1", "1", userId);

	@BeforeEach
	void setUp() throws Exception {
		feedDTO = new FeedDTO();
		feedDTO.setFeedId("123");
		feedDTO.setUserId("111");
		feedDTO.setUserName("Eleven");
		feedDTO.setEmail("eleven.11@gmail.com");
		feedDTO.setUserName("Test");
		auditDTO = new AuditDTO();

		User mockUser = new User();
		mockUser.setEmail("eleven.11@gmail.com");
		mockUser.setPassword("111111");
		mockUser.setUserId("12345");
		when(jsonReader.getActiveUserDetails("12345", "mock.jwt.token")).thenReturn(mockUser);

		when(jwtService.extractUserID("mock.jwt.token")).thenReturn(userId);

		UserDetails mockUserDetails = mock(UserDetails.class);
		when(jwtService.getUserDetail(anyString())).thenReturn(mockUserDetails);

		when(jwtService.validateToken(anyString(), eq(mockUserDetails))).thenReturn(true);

		when(jwtService.getUserIdByAuthHeader(authorizationHeader)).thenReturn(userId);

	}

	@Test
	void testGetByUserId() throws Exception {

		int page = 0;
		int size = 50;

		List<FeedDTO> mockFeeds = new ArrayList<>();

		Map<Long, List<FeedDTO>> resultMap = new HashMap<>();
		resultMap.put(10L, mockFeeds);

		when(feedService.getFeedsByUserWithPagination(userId, page, size)).thenReturn(resultMap);

		when(auditService.createAuditDTO(userId, "Feed List by User", activityTypePrefix, "/api/feeds/users",
				HTTPVerb.POST)).thenReturn(auditDTO);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/users").header("Authorization", authorizationHeader)
				.param("page", String.valueOf(page)).param("size", String.valueOf(size))
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(mockFeeds.size()))
				.andExpect(jsonPath("$.message").value("Successfully get all feeds by Users"))
				.andExpect(jsonPath("$.totalRecord").value(10)).andDo(print());

		resultMap = new HashMap<>();

		when(feedService.getFeedsByUserWithPagination(userId, page, size)).thenReturn(resultMap);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/users").header("Authorization", authorizationHeader)
				.param("page", String.valueOf(page)).param("size", String.valueOf(size))
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray())
				.andExpect(jsonPath("$.data.length()").value(mockFeeds.size()))
				.andExpect(jsonPath("$.message").value("Feeds not found."))
				.andExpect(jsonPath("$.totalRecord").value(0)).andDo(print());

		apiRequest.setUserId("");
		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/users").header("Authorization", authorizationHeader)
				.param("page", String.valueOf(page)).param("size", String.valueOf(size))
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("Bad Request:User could not be blank.")).andDo(print());

		apiRequest.setUserId("123");
		auditDTO = new AuditDTO();

		String message = "An unexpected error occurred. Please contact support.";

		auditDTO.setRemarks(message);

		doNothing().when(auditService).logAudit(any(AuditDTO.class), eq(500), anyString(), eq(authorizationHeader));

		when(auditService.createAuditDTO(anyString(), anyString(), anyString(), anyString(), any()))
				.thenReturn(auditDTO); // Ensure a non-null AuditDTO is returned

		when(feedService.getFeedsByUserWithPagination(anyString(), anyInt(), anyInt()))
				.thenThrow(new RuntimeException("Unexpected error occurred"));

		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/users").header("Authorization", authorizationHeader)
				.param("page", String.valueOf(page)).param("size", String.valueOf(size))
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."))
				.andDo(print());

		// Verify audit logging was called
		verify(auditService, times(1)).logAudit(any(AuditDTO.class), eq(500), anyString(), eq(authorizationHeader));
	}

	@Test
	public void testGetFeedById() throws Exception {
		String feedId = "123";
		apiRequest.setFeedId(feedId);

		when(auditService.createAuditDTO(userId, "Find Feed by Id", activityTypePrefix, "/api/feeds/Id", HTTPVerb.POST))
				.thenReturn(auditDTO);

		when(feedService.findByFeedId(feedId)).thenReturn(feedDTO);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/Id").header("Authorization", authorizationHeader)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value("true"))
				.andExpect(jsonPath("$.data.feedId").value(feedId))
				.andExpect(jsonPath("$.message").value("Feed get successfully.")).andDo(print());

		feedDTO = new FeedDTO();
		when(feedService.findByFeedId(feedId)).thenReturn(feedDTO);
		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/Id").header("Authorization", authorizationHeader)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value("false"))
				.andExpect(jsonPath("$.message").value("Feed not found for Id: " + feedId)).andDo(print());

		apiRequest.setFeedId("");
		feedDTO = new FeedDTO();
		when(feedService.findByFeedId(apiRequest.getFeedId())).thenReturn(feedDTO);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/Id").header("Authorization", authorizationHeader)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value("false"))
				.andExpect(jsonPath("$.message").value("Bad Request:FeedId could not be blank.")).andDo(print());

		apiRequest.setFeedId("123");
		auditDTO = new AuditDTO();

		String message = "An unexpected error occurred. Please contact support.";

		auditDTO.setRemarks(message);

		doNothing().when(auditService).logAudit(any(AuditDTO.class), eq(500), anyString(), eq(authorizationHeader));
		
		when(feedService.findByFeedId(anyString()))
		.thenThrow(new RuntimeException("Unexpected error occurred"));
		
		when(auditService.createAuditDTO(anyString(), anyString(), anyString(), anyString(), any()))
				.thenReturn(auditDTO);

		mockMvc.perform(MockMvcRequestBuilders.post("/api/feeds/Id").header("Authorization", authorizationHeader)
				.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."))
				.andDo(print());

		// Verify audit logging was called
		verify(auditService, times(1)).logAudit(any(AuditDTO.class), eq(500), anyString(), eq(authorizationHeader));

	}

	@Test
	public void testPatchFeedReadStatus() throws Exception {
		String feedId = "123";
		feedDTO.setIsReaded("1");
		apiRequest.setFeedId(feedId);

		when(auditService.createAuditDTO(userId, "Update Feed Status", activityTypePrefix, "/api/feeds/readStatus",
				HTTPVerb.PATCH)).thenReturn(auditDTO);

		when(feedService.updateReadStatusById(feedId)).thenReturn(feedDTO);

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/feeds/readStatus").header("Authorization", authorizationHeader)
						.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isOk()).andExpect(jsonPath("$.success").value("true"))
				.andExpect(jsonPath("$.data.feedId").value(feedId))
				.andExpect(jsonPath("$.message").value("Read status updated successfully for Id: " + feedId));

		feedDTO = new FeedDTO();
		when(feedService.updateReadStatusById(feedId)).thenReturn(feedDTO);
		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/feeds/readStatus").header("Authorization", authorizationHeader)
						.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isNotFound()).andExpect(jsonPath("$.success").value("false"))
				.andExpect(jsonPath("$.message").value("Feed not found for Id: " + feedId));

		apiRequest.setFeedId("");
		when(apiRequest.getFeedId()).thenReturn("");

		mockMvc.perform(
				MockMvcRequestBuilders.patch("/api/feeds/readStatus").header("Authorization", authorizationHeader)
						.contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(apiRequest)))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value("false"))
				.andExpect(jsonPath("$.message").value("Bad Request:FeedId could not be blank."));
		
		
		apiRequest.setFeedId("123");
		
		auditDTO = new AuditDTO();

		String message = "An unexpected error occurred. Please contact support.";

		auditDTO.setRemarks(message);

		doNothing().when(auditService).logAudit(any(AuditDTO.class), eq(500), anyString(), eq(authorizationHeader));
		when(feedService.updateReadStatusById(feedId)).thenThrow(new RuntimeException("Unexpected error"));

		when(auditService.createAuditDTO(anyString(), anyString(), anyString(), anyString(), any()))
				.thenReturn(auditDTO);
		

        mockMvc.perform(MockMvcRequestBuilders.patch("/api/feeds/readStatus")
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(apiRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred. Please contact support."));

       
	}

}
