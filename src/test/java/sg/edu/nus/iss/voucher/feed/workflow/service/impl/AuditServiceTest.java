package sg.edu.nus.iss.voucher.feed.workflow.service.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import io.jsonwebtoken.JwtException;
import sg.edu.nus.iss.voucher.feed.workflow.aws.service.SQSPublishingService;
import sg.edu.nus.iss.voucher.feed.workflow.dto.AuditDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.AuditResponseStatus;
import sg.edu.nus.iss.voucher.feed.workflow.entity.HTTPVerb;
import sg.edu.nus.iss.voucher.feed.workflow.jwt.JWTService;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuditServiceTest {

    @InjectMocks
    private AuditService auditService;

    @Mock
    private SQSPublishingService sqsPublishingService;

    @Mock
    private JWTService jwtService;

    private final String dummyToken = "Bearer dummy.jwt.token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendMessage_success() throws JwtException, IllegalArgumentException, Exception {
        String expectedUsername = "john.doe";
        AuditDTO auditDTO = new AuditDTO();
        when(jwtService.retrieveUserName("dummy.jwt.token")).thenReturn(expectedUsername);

        auditService.sendMessage(auditDTO, dummyToken);

        verify(jwtService, times(1)).retrieveUserName("dummy.jwt.token");
        verify(sqsPublishingService, times(1)).sendMessage(auditDTO);
    }

    @Test
    void testSendMessage_emptyToken() {
        AuditDTO auditDTO = new AuditDTO();
        auditService.sendMessage(auditDTO, "Bearer ");
        verify(sqsPublishingService).sendMessage(auditDTO);
    }

    @Test
    void testCreateAuditDTO() {
        String userId = UUID.randomUUID().toString();
        String activityType = "CREATE";
        String prefix = "AUDIT_";
        String endpoint = "/api/test";
        HTTPVerb verb = HTTPVerb.POST;

        AuditDTO dto = auditService.createAuditDTO(userId, activityType, prefix, endpoint, verb);

        assertNotNull(dto);
        assertEquals(userId, dto.getUserId());
       
    }

    @Test
    void testLogAudit_successStatus() throws JwtException, IllegalArgumentException, Exception {
        AuditDTO dto = new AuditDTO();
        String msg = "Operation succeeded";
        when(jwtService.retrieveUserName(any())).thenReturn("testUser");

        auditService.logAudit(dto, 200, msg, dummyToken);


        verify(sqsPublishingService, times(1)).sendMessage(dto);
    }

    @Test
    void testLogAudit_failedStatus() throws JwtException, IllegalArgumentException, Exception {
        AuditDTO dto = new AuditDTO();
        String msg = "Failed due to error";
        when(jwtService.retrieveUserName(any())).thenReturn("testUser");

        auditService.logAudit(dto, 500, msg, dummyToken);


        verify(sqsPublishingService, times(1)).sendMessage(dto);
    }
    
    @Test
    void testSendMessage_whenSQSThrowsException_logsError() throws JwtException, IllegalArgumentException, Exception {
        AuditDTO auditDTO = new AuditDTO();
        String expectedUsername = "jane.doe";
        when(jwtService.retrieveUserName("dummy.jwt.token")).thenReturn(expectedUsername);

        // Simulate exception thrown by SQS publisher
        doThrow(new RuntimeException("SQS Failure"))
                .when(sqsPublishingService).sendMessage(any(AuditDTO.class));

        auditService.sendMessage(auditDTO, dummyToken);

        // Verify that jwtService still gets called
        verify(jwtService).retrieveUserName("dummy.jwt.token");
        // Verify sendMessage was attempted
        verify(sqsPublishingService).sendMessage(auditDTO);
    }

    
}
