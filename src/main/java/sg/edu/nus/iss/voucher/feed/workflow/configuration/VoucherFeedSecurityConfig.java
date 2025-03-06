package sg.edu.nus.iss.voucher.feed.workflow.configuration;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.HstsHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
public class VoucherFeedSecurityConfig {

    private static final String[] SECURED_URLS = { "/api/feeds/**", "/ws/liveFeeds/**" };

    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    return http.cors(cors -> {
	        cors.configurationSource(request -> {
	            CorsConfiguration config = new CorsConfiguration();
	            config.setAllowedOrigins(List.of("*"));
	            config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "OPTIONS"));
	            config.setAllowedHeaders(List.of("*"));
	            config.applyPermitDefaultValues();
	            return config;
	        });
	    }).headers(headers -> headers
	        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Origin", "*"))
	        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Methods", "GET, POST, PUT, PATCH, OPTIONS"))
	        .addHeaderWriter(new StaticHeadersWriter("Access-Control-Allow-Headers", "*"))
	        .addHeaderWriter(new HstsHeaderWriter(31536000, false, true))
	        .addHeaderWriter((request, response) -> {
	            response.addHeader("Cache-Control", "max-age=60, must-revalidate");
	        })
	    ).csrf(AbstractHttpConfigurer::disable)
	    .authorizeHttpRequests(auth -> auth
	        .requestMatchers(SECURED_URLS).permitAll()
	        .anyRequest().authenticated()
	    )
	    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	    .build();
	}

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

   
}
