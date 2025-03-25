package sg.edu.nus.iss.voucher.feed.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FeedRequest {
	private String feedId="";
	private String campaignId="";
	private String userId="";

}
