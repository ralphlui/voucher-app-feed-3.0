package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.*;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.SubscribeRequest;

import sg.edu.nus.iss.voucher.feed.workflow.utility.GeneralUtility;

@SpringBootTest
@ActiveProfiles("test")
class SQSQueueHandlerTest {

    @InjectMocks
    private SQSQueueHandler sqsQueueHandler;

    @Mock
    private AmazonSQS amazonSQS;

    @Mock
    private AmazonSNS amazonSNS;

    @Mock
    private SNSSubscriptionService snsSubscriptionService;

    @BeforeEach
    void setup() {
        sqsQueueHandler.queueName = "test-queue";
        sqsQueueHandler.sqsArnPrefix = "arn:aws:sqs:ap-southeast-1:123456789012:";
        sqsQueueHandler.topicArn = "arn:aws:sns:ap-southeast-1:123456789012:test-topic";
    }

    @Test
    void testDoesQueueExist_shouldReturnTrue() {
        ListQueuesResult result = new ListQueuesResult().withQueueUrls("https://sqs.aws.com/test-queue");
        when(amazonSQS.listQueues(any(ListQueuesRequest.class))).thenReturn(result);

        boolean exists = sqsQueueHandler.doesQueueExist("test-queue");
        assertTrue(exists);
    }

    @Test
    void testDoesQueueExist_shouldReturnFalse() {
        ListQueuesResult result = new ListQueuesResult().withQueueUrls("https://sqs.aws.com/another-queue");
        when(amazonSQS.listQueues(any(ListQueuesRequest.class))).thenReturn(result);

        boolean exists = sqsQueueHandler.doesQueueExist("test-queue");
        assertFalse(exists);
    }

    @Test
    void testConsumeMessages_shouldProcessAndDeleteMessages() {
        String queueUrl = "https://sqs.aws.com/test-queue";

        when(amazonSQS.listQueues(any(ListQueuesRequest.class)))
            .thenReturn(new ListQueuesResult().withQueueUrls(queueUrl));
        when(amazonSQS.getQueueUrl("test-queue"))
            .thenReturn(new GetQueueUrlResult().withQueueUrl(queueUrl));
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(new ReceiveMessageResult().withMessages(
                new Message().withBody("message-body").withReceiptHandle("receipt-handle")
            ));
        when(snsSubscriptionService.processNotification("message-body"))
            .thenReturn("processed-message");

        sqsQueueHandler.consumeMessages();

        verify(amazonSQS).deleteMessage(new DeleteMessageRequest(queueUrl, "receipt-handle"));
    }

    @Test
    void testProcessFeedData_shouldCallSubscriptionService() {
        when(snsSubscriptionService.processNotification("test-msg"))
            .thenReturn("processed");

        sqsQueueHandler.processFeedData("test-msg");

        verify(snsSubscriptionService).processNotification("test-msg");
    }

    @Test
    void testCreateQueueIfNotExists_queueExists() {
        String queueUrl = "https://sqs.aws.com/test-queue";
        String queueArn = "arn:aws:sqs:ap-southeast-1:123456789012:test-queue";

        try (MockedStatic<GeneralUtility> util = mockStatic(GeneralUtility.class)) {
            when(amazonSQS.getQueueUrl("test-queue")).thenReturn(new GetQueueUrlResult().withQueueUrl(queueUrl));
            when(amazonSQS.getQueueAttributes(any(GetQueueAttributesRequest.class)))
                .thenReturn(new GetQueueAttributesResult().withAttributes(Map.of("QueueArn", queueArn)));
            util.when(() -> GeneralUtility.makeNotNull(queueUrl)).thenReturn(queueUrl);

            sqsQueueHandler.createQueueIfNotExists();

            verify(amazonSNS).subscribe(new SubscribeRequest(
                sqsQueueHandler.topicArn, "sqs", queueArn));
        }
    }

    @Test
    void testCreateQueueIfNotExists_queueDoesNotExist() {
        String createdUrl = "https://sqs.aws.com/test-queue";
        String queueArn = "arn:aws:sqs:ap-southeast-1:123456789012:test-queue";

        try (MockedStatic<GeneralUtility> util = mockStatic(GeneralUtility.class)) {
            when(amazonSQS.getQueueUrl("test-queue"))
                .thenThrow(new QueueDoesNotExistException("Queue does not exist"));

            when(amazonSQS.createQueue(any(CreateQueueRequest.class)))
                .thenReturn(new CreateQueueResult().withQueueUrl(createdUrl));

            when(amazonSQS.getQueueAttributes(any(GetQueueAttributesRequest.class)))
                .thenReturn(new GetQueueAttributesResult().withAttributes(Map.of("QueueArn", queueArn)));

            util.when(() -> GeneralUtility.makeNotNull(createdUrl)).thenReturn(createdUrl);

            sqsQueueHandler.createQueueIfNotExists();

            verify(amazonSQS).createQueue(any(CreateQueueRequest.class));
            verify(amazonSNS).subscribe(new SubscribeRequest(
                sqsQueueHandler.topicArn, "sqs", queueArn));
        }
    }
}
