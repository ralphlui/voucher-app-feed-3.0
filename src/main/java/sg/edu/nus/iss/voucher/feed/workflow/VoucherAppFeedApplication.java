package sg.edu.nus.iss.voucher.feed.workflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VoucherAppFeedApplication {

	public static void main(String[] args) {
		SpringApplication.run(VoucherAppFeedApplication.class, args);
	}
	
}
