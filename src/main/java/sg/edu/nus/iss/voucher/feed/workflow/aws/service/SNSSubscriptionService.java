package sg.edu.nus.iss.voucher.feed.workflow.aws.service;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import sg.edu.nus.iss.voucher.feed.workflow.dao.FeedDAO;
import sg.edu.nus.iss.voucher.feed.workflow.dto.FeedDTO;
import sg.edu.nus.iss.voucher.feed.workflow.entity.*;
import sg.edu.nus.iss.voucher.feed.workflow.pojo.User;
import sg.edu.nus.iss.voucher.feed.workflow.strategy.impl.EmailStrategy;
import sg.edu.nus.iss.voucher.feed.workflow.utility.*;

@Service
public class SNSSubscriptionService {

	@Autowired
	private JSONReader jsonReader;

	@Autowired
	private FeedDAO feedDAO;

	@Autowired
	private EmailStrategy emailStrategy;


	private static final Logger logger = LoggerFactory.getLogger(SNSSubscriptionService.class);


	public String processNotification(String snsMessage) {
		String retMsg = "";
		try {
			JsonNode jsonNode = new ObjectMapper().readTree(snsMessage);
			String message = jsonNode.get("Message").asText();
			logger.info("Received message: {}", message);

			if (message == null || message.isEmpty()) {
				logger.info("Message is null or empty.");
				return retMsg = "Bad Request:Message is null or empty.";
			}

			MessagePayload feedMsg = jsonReader.readFeedMessage(message);
			if (feedMsg == null) {
				logger.error("Failed to parse the feed message. The message payload is null or invalid.");
				return retMsg = "Bad Request:Failed to parse the feed message. The message payload is null or invalid.";
			}

			retMsg += "Processed user:" ;
			/*ArrayList<User> users = getAllActiveUsers(token);
			

			logger.info("Processing target users: {}", users);
			retMsg = "Processed user:";
			for (User user : users) {
				boolean processed = generatFeed(user, feedMsg);
				logger.info("Processed user: {} with result: {}", user.getUserId(), processed);
				retMsg += user.getUserId() + ":" + processed ;

			}*/

		} catch (Exception ex) {
			logger.error("Bad Request:Process Message Exception: {}", ex.toString(), ex);
		}
		return retMsg;
	}

	ArrayList<User> getAllActiveUsers(String token) {

		ArrayList<User> users = jsonReader.getAllActiveUsers(token);
		return users;
	}

	public boolean generatFeed(User user, MessagePayload feedMsg) {

		try {
			String userId = GeneralUtility.makeNotNull(user.getUserId()).trim();
			logger.info("userId: {}", userId);

			if (userId.isEmpty()) {
				return false;
			}

			// Check checkFeedExistsByUserAndCampaign
			boolean isExists = feedDAO.checkFeedExistsByUserAndCampaign(user.getUserId(),
					feedMsg.getCampaignId());
			logger.info("checkFeedExistsByUserAndCampaign: {}", isExists);
			//
			if (!isExists) {
				Feed feed = createFeed(feedMsg, user);

				Feed savedFeed = feedDAO.saveFeed(feed);
				if (savedFeed.getFeedId().isEmpty()) {
					return false;
				}else {
					FeedDTO feedDTO=  DTOMapper.toFeedDTO(savedFeed);
					
					return sendNotifications(feedDTO);
				}

				
			} else {
				return true;
			}

		} catch (Exception ex) {
			logger.error("Process Message Exception: {}", ex.toString(), ex);
		}
		return false;
	}

	private Feed createFeed(MessagePayload feedMsg, User user) throws Exception {

		Feed feed = new Feed();
		feed.setUserId(user.getUserId());
		feed.setEmail(user.getEmail());
		feed.setUserName(user.getUsername());
		feed.setCampaignId(GeneralUtility.makeNotNull(feedMsg.getCampaignId()));
		feed.setCampaignDescription(GeneralUtility.makeNotNull(feedMsg.getCampaignDescription()));
		feed.setStoreId(GeneralUtility.makeNotNull(feedMsg.getStoreId()));
		feed.setStoreName(GeneralUtility.makeNotNull(feedMsg.getStoreName()));
		return feed;
	}

	private boolean sendNotifications(FeedDTO feedDTO) {
		
		boolean isSendEmail = emailStrategy.sendNotification(feedDTO);
		logger.info("isSendEmail: {}", isSendEmail);

		return isSendEmail;
	}

}
