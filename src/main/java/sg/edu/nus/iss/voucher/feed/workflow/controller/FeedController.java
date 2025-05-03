package sg.edu.nus.iss.voucher.feed.workflow.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import sg.edu.nus.iss.voucher.feed.workflow.dto.*;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.AuditService;
import sg.edu.nus.iss.voucher.feed.workflow.service.impl.FeedService;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@RestController
@Validated
@RequestMapping("/api/feeds")
public class FeedController {
	private static final Logger logger = LoggerFactory.getLogger(FeedController.class);
	private static final String UNEXPECTED_ERROR = "An unexpected error occurred. Please contact support.";
	private static final String LOG_MESSAGE_FORMAT = "{} {}";

	@Autowired
	private FeedService feedService;

	@Autowired
	private AuditService auditService;


	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;
	
	private static final String INVALID_USER_ID = "Invalid UserID";


	@PostMapping(value = "/users", produces = "application/json")
	public ResponseEntity<APIResponse<List<FeedDTO>>> getByUserId(
	        @RequestHeader(value = "Authorization", required = true) String authorizationHeader,
	        @RequestBody FeedRequest apiRequest,
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "50") int size) {

	    logger.info("Call feeds by UserId feed API...");

	    String message = "";
	    String activityType = "Feed List by User";
	    String endpoint = "/api/feeds/users/";
	    HTTPVerb httpMethod = HTTPVerb.POST;

	    AuditDTO auditDTO = auditService.createAuditDTO(INVALID_USER_ID, activityType, activityTypePrefix, endpoint, httpMethod);

	    try {
	        String userId = GeneralUtility.makeNotNull(apiRequest.getUserId()).trim();
	        if (!userId.isEmpty()) {

	            Map<Long, List<FeedDTO>> resultMap = feedService.getFeedsByUserWithPagination(userId, page, size);
	            List<FeedDTO> feedDTOList = new ArrayList<>();
	            long totalRecord = 0;

	            if (resultMap.isEmpty()) {
	                message = "Feeds not found.";
	                logger.warn(message);
	                auditService.logAudit(auditDTO, 200, message, authorizationHeader);
	                return ResponseEntity.ok(APIResponse.success(feedDTOList, message, totalRecord));
	            }

	            for (Map.Entry<Long, List<FeedDTO>> entry : resultMap.entrySet()) {
	                totalRecord = entry.getKey();
	                feedDTOList = entry.getValue();
	                logger.info("totalRecord: {}", totalRecord);
	                logger.info("FeedDTO List: {}", feedDTOList);
	            }

	            message = "Successfully retrieved all feeds by user.";
	            auditService.logAudit(auditDTO, 200, message, authorizationHeader);
	            return ResponseEntity.ok(APIResponse.success(feedDTOList, message, totalRecord));
	        } else {
	            message = "Bad Request: UserId cannot be blank.";
	            logger.error(message);
	            auditService.logAudit(auditDTO, 400, message, authorizationHeader);
	            return ResponseEntity.badRequest().body(APIResponse.error(message));
	        }

	    } catch (Exception e) {
	        message = UNEXPECTED_ERROR;
	        logger.error(LOG_MESSAGE_FORMAT, message, e.getMessage());
	        auditDTO.setRemarks(e.getMessage());
	        auditService.logAudit(auditDTO, 500, message, authorizationHeader);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
	    }
	}

	@PostMapping(value = "/Id", produces = "application/json")
	public ResponseEntity<APIResponse<FeedDTO>> getFeedById(
	        @RequestHeader(value = "Authorization", required = true) String authorizationHeader,
	        @RequestBody FeedRequest apiRequest) {

	    logger.info("Calling getById Feed API...");
	    String message = "";
	    String activityType = "Find Feed by Id";
	    String endpoint = "/api/feeds/";
	    HTTPVerb httpMethod = HTTPVerb.POST;

	    AuditDTO auditDTO = auditService.createAuditDTO(INVALID_USER_ID, activityType, activityTypePrefix, endpoint, httpMethod);

	    try {
	        String feedId = GeneralUtility.makeNotNull(apiRequest.getFeedId()).trim();
	        logger.info("feedId: {}", feedId);

	        if (!feedId.isEmpty()) {
	            FeedDTO feedDTO = feedService.findByFeedId(feedId);

	            if (feedDTO != null && feedId.equals(GeneralUtility.makeNotNull(feedDTO.getFeedId()))) {
	                message = "Feed retrieved successfully.";
	                auditService.logAudit(auditDTO, 200, message, authorizationHeader);
	                return ResponseEntity.ok(APIResponse.success(feedDTO, message));
	            } else {
	                message = "Feed not found for Id: " + feedId;
	                auditService.logAudit(auditDTO, 404, message, authorizationHeader);
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
	            }

	        } else {
	            message = "Bad Request: FeedId cannot be blank.";
	            logger.error(message);
	            auditService.logAudit(auditDTO, 400, message, authorizationHeader);
	            return ResponseEntity.badRequest().body(APIResponse.error(message));
	        }

	    } catch (Exception e) {
	        message = UNEXPECTED_ERROR;
	        logger.error(LOG_MESSAGE_FORMAT, message, e.getMessage());
	        auditDTO.setRemarks(e.getMessage());
	        auditService.logAudit(auditDTO, 500, message, authorizationHeader);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
	    }
	}

	
	@PatchMapping(value = "/readStatus", produces = "application/json")
	public ResponseEntity<APIResponse<FeedDTO>> patchFeedReadStatus(
	        @RequestHeader(value = "Authorization", required = true) String authorizationHeader,
	        @RequestBody FeedRequest apiRequest) {

	    logger.info("Calling updateReadStatusById Feed API...");
	    String message = "";
	    String activityType = "Update Feed Status";
	    String endpoint = "/api/feeds/readStatus";
	    HTTPVerb httpMethod = HTTPVerb.PATCH;

	    AuditDTO auditDTO = auditService.createAuditDTO(INVALID_USER_ID, activityType, activityTypePrefix, endpoint, httpMethod);

	    try {
	        String feedId = GeneralUtility.makeNotNull(apiRequest.getFeedId()).trim();
	        logger.info("feedId: {}", feedId);

	        if (!feedId.isEmpty()) {
	            FeedDTO feedDTO = feedService.updateReadStatusById(feedId);

	            if (feedId.equals(GeneralUtility.makeNotNull(feedDTO.getFeedId()))) {
	                message = "Read status updated successfully for Id: " + feedId;
	                auditService.logAudit(auditDTO, 200, message, authorizationHeader);
	                return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(feedDTO, message));
	            } else {
	                message = "Feed not found for Id: " + feedId;
	                auditService.logAudit(auditDTO, 404, message, authorizationHeader);
	                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
	            }
	        } else {
	            message = "Bad Request: FeedId could not be blank.";
	            auditService.logAudit(auditDTO, 400, message, authorizationHeader);
	            logger.error(message);
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
	        }

	    } catch (Exception e) {
	        message = UNEXPECTED_ERROR;

	        logger.error(LOG_MESSAGE_FORMAT, message, e.getMessage());
	        auditDTO.setRemarks(e.getMessage()); 
	        auditService.logAudit(auditDTO, 500, message, authorizationHeader);

	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
	    }
	}


}
