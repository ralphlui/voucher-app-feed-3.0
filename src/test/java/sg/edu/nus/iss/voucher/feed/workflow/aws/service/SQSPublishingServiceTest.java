package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;

@SpringBootTest
@ActiveProfiles("test")
class SQSPublishingServiceTest {

    @Mock
    private AmazonSQS amazonSQS;

    @InjectMocks
    private SQSPublishingService sqsPublishingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMessage_shouldSendMessageToSQS() throws Exception {
        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRemarks("Test message");

        when(amazonSQS.sendMessage(any(SendMessageRequest.class))).thenReturn(new SendMessageResult());

        sqsPublishingService.sendMessage(auditDTO);

        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(amazonSQS, times(1)).sendMessage(captor.capture());

        SendMessageRequest sentRequest = captor.getValue();
        assertNotNull(sentRequest);
        assertTrue(sentRequest.getMessageBody().contains("Test message"));
    }


    @Test
    void truncateMessage_whenMessageExceedsLimit_shouldReturnTruncatedMessage() {
        String remarks = "This is a very long remark that will be truncated";
        String currentMessage = "{\"remarks\":\"" + remarks + "\"}";
        int maxMessageSize = 3 * 4;

        String truncatedRemarks = sqsPublishingService.truncateMessage(remarks, maxMessageSize, currentMessage);

        assertNotNull(truncatedRemarks);
        assertNotEquals(remarks, truncatedRemarks);
       // assertTrue(truncatedRemarks.length() < remarks.length());
    }
    
    
    @Test
    void sendMessageTruncateAndSend_whenMessageSizeExceedsLimit() throws Exception {
        
        StringBuilder longRemark = new StringBuilder();
        for (int i = 0; i < 300000; i++) { // 300,000 characters ~ 300 KB
            longRemark.append("A");
        }

        AuditDTO auditDTO = new AuditDTO();
        auditDTO.setRemarks(longRemark.toString());

        when(amazonSQS.sendMessage(any(SendMessageRequest.class))).thenReturn(new SendMessageResult());

        sqsPublishingService.sendMessage(auditDTO);

        
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        verify(amazonSQS, times(1)).sendMessage(captor.capture());

        SendMessageRequest sentRequest = captor.getValue();
        assertNotNull(sentRequest);
 
        assertTrue(sentRequest.getMessageBody().getBytes(StandardCharsets.UTF_8).length <= 256 * 1024);
 
        assertTrue(sentRequest.getMessageBody().contains("A..."));
    }
    
    @Test
    void truncateMessageExceptionOccurs() {
          
        String remarks = null;
        String currentMessage = "{\"remarks\":\"Valid message\"}";
        int maxMessageSize = 256 * 1024; // 256 KB
 
        String result = sqsPublishingService.truncateMessage(remarks, maxMessageSize, currentMessage);
 
        assertNull(result);

       
    }
    
    @Test
    void truncateMessageExceedLimit() {
        // Given
        String remarks = "Short message"; // Small enough to not be truncated
        String currentMessage = "{\"remarks\":\"Short message\"}"; // Message is within limit
        int maxMessageSize = 256 * 1024; // 256 KB

        // When
        String result = sqsPublishingService.truncateMessage(remarks, maxMessageSize, currentMessage);

        // Then
        assertEquals(remarks, result, "Remarks should remain unchanged when within the size limit");
    }
    @Test
    void truncateMessage_whenJsonProcessingFails_shouldReturnOriginalRemarks() {
        String remarks = "Valid message";
        String invalidJson = "{invalid json}"; // Malformed JSON
        int maxMessageSize = 256 * 1024;

        String result = sqsPublishingService.truncateMessage(remarks, maxMessageSize, invalidJson);
        assertEquals(remarks, result, "Should return original remarks when JSON processing fails");
    }



}
