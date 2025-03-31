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

	@Autowired
	private FeedService feedService;

	@Autowired
	private AuditService auditService;


	@Value("${audit.activity.type.prefix}")
	String activityTypePrefix;

	@PostMapping(value = "/users", produces = "application/json")
	public ResponseEntity<APIResponse<List<FeedDTO>>> getByUserId(
			@RequestHeader(value = "Authorization", required = true) String authorizationHeader,
			@RequestBody FeedRequest apiRequest, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {

		logger.info("Call feeds by UserId feed API...");

		String message = "";
		String activityType = "Feed List by User";
		String endpoint = "/api/feeds/users/";
		HTTPVerb httpMethod = HTTPVerb.POST;

		String tokenUserId = "Invalid UserID";

		AuditDTO auditDTO = auditService.createAuditDTO(tokenUserId, activityType, activityTypePrefix, endpoint,
				httpMethod);

		try {

			String userId = GeneralUtility.makeNotNull(apiRequest.getUserId()).trim();
			if (!userId.equals("")) {

				Map<Long, List<FeedDTO>> resultMap = feedService.getFeedsByUserWithPagination(userId, page, size);
				List<FeedDTO> feedDTOList = new ArrayList<FeedDTO>();
				long totalRecord = 0;
				if (resultMap.size() == 0) {
					String mesasge = "Feeds not found.";
					logger.error(mesasge);
					auditService.logAudit(auditDTO, 200, message, authorizationHeader);

					return ResponseEntity.status(HttpStatus.OK)
							.body(APIResponse.success(feedDTOList, mesasge, totalRecord));
				}
				for (Map.Entry<Long, List<FeedDTO>> entry : resultMap.entrySet()) {
					totalRecord = entry.getKey();
					feedDTOList = entry.getValue();
					logger.info("totalRecord: " + totalRecord);
					logger.info("FeedDTO List: " + feedDTOList);
				}

				message = "Successfully get all feeds by Users";
				auditService.logAudit(auditDTO, 200, message, authorizationHeader);

				return ResponseEntity.status(HttpStatus.OK)
						.body(APIResponse.success(feedDTOList, message, totalRecord));
			} else {
				message = "Bad Request:User could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO, 400, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}

		} catch (Exception e) {
			message = "An unexpected error occurred. Please contact support.";
			logger.error(message);
			auditDTO.setRemarks(e.toString());
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

		String tokenUserId = "Invalid UserID";

		AuditDTO auditDTO = auditService.createAuditDTO(tokenUserId, activityType, activityTypePrefix, endpoint,
				httpMethod);

		try {

			String feedId = GeneralUtility.makeNotNull(apiRequest.getFeedId()).trim();
			logger.info("feedId: " + feedId);
			if (!GeneralUtility.makeNotNull(feedId).equals("")) {
				FeedDTO feedDTO = feedService.findByFeedId(feedId);
				if (feedDTO != null && GeneralUtility.makeNotNull(feedDTO.getFeedId()).equals(feedId)) {
					message = "Feed get successfully.";
					auditService.logAudit(auditDTO, 200, message, authorizationHeader);

					return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(feedDTO, message));

				} else {
					message = "Feed not found for Id: " + feedId;
					auditService.logAudit(auditDTO, 404, message, authorizationHeader);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}

			} else {
				message = "Bad Request:FeedId could not be blank.";
				logger.error(message);
				auditService.logAudit(auditDTO, 400, message, authorizationHeader);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}

		} catch (Exception e) {
			
			message = "An unexpected error occurred. Please contact support.";
			logger.error(message + e.toString());
			auditDTO.setRemarks(e.toString());
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

		String tokenUserId = "Invalid UserID";

		AuditDTO auditDTO = auditService.createAuditDTO(tokenUserId, activityType, activityTypePrefix, endpoint,
				httpMethod);

		try {

			String feedId = GeneralUtility.makeNotNull(apiRequest.getFeedId()).trim();
			logger.info("feedId: " + feedId);

			if (!GeneralUtility.makeNotNull(feedId).equals("")) {
				FeedDTO feedDTO = feedService.updateReadStatusById(feedId);

				if (GeneralUtility.makeNotNull(feedDTO.getFeedId()).equals(feedId)) {
					message = "Read status updated successfully for Id: " + feedId;
					auditService.logAudit(auditDTO, 200, message, authorizationHeader);
					return ResponseEntity.status(HttpStatus.OK).body(APIResponse.success(feedDTO, message));
				} else {
					message = "Feed not found for Id: " + feedId;
					auditService.logAudit(auditDTO, 404, message, authorizationHeader);
					return ResponseEntity.status(HttpStatus.NOT_FOUND).body(APIResponse.error(message));
				}
			} else {
				message = "Bad Request:FeedId could not be blank.";
				auditService.logAudit(auditDTO, 400, message, authorizationHeader);
				logger.error(message);
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(APIResponse.error(message));
			}

		} catch (Exception e) {
			
			message = "An unexpected error occurred. Please contact support.";
			logger.error(message + e.toString());
			auditDTO.setRemarks(e.toString());
			auditService.logAudit(auditDTO, 500, message, authorizationHeader);

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(APIResponse.error(message));
		}
	}

}
