package sg.edu.nus.iss.voucher.feed.workflow.jwt;


import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TokenErrorResponseTest {

    @Test
    void testSendErrorResponse() throws Exception {
   
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(mockResponse.getWriter()).thenReturn(printWriter);

        String expectedMessage = "Invalid token";
        int expectedStatus = HttpServletResponse.SC_UNAUTHORIZED; 
        String expectedError = "Unauthorized";

        TokenErrorResponse.sendErrorResponse(mockResponse, expectedMessage, expectedStatus, expectedError);

        printWriter.flush();

        verify(mockResponse).setStatus(expectedStatus);
        verify(mockResponse).setContentType("application/json");

        ObjectMapper objectMapper = new ObjectMapper();
        String jsonOutput = stringWriter.toString();

        Map<String, Object> responseMap = objectMapper.readValue(jsonOutput, Map.class);

        assertEquals(false, responseMap.get("success"));
        assertEquals(expectedMessage, responseMap.get("message"));
        assertEquals(0, responseMap.get("totalRecord"));
        assertNull(responseMap.get("data"));
        assertEquals(expectedStatus, responseMap.get("status"));
    }
}

