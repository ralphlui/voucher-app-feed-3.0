package sg.edu.nus.iss.voucher.feed.workflow.utility;

import org.springframework.stereotype.Component;

@Component
public class GeneralUtility {

	public static String makeNotNull(Object str) {
		if (str == null) {
			return "";
		} else if (str.equals("null")) {
			return "";
		} else {
			return str.toString();
		}
	}

}
