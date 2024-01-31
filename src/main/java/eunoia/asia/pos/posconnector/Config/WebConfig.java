package eunoia.asia.pos.posconnector.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@EnableWebMvc
public class WebConfig extends WebMvcConfigurerAdapter {

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**")
				.allowedOrigins(
						"http://localhost:8080",
						"https://pos-staging.eunoia.asia",
						"https://pos-uat.eunoia.asia",
						"https://pos.eunoia.asia"
				);
	}
}
