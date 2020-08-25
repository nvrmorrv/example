package webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import webapp.configs.AppConfig;

@Configuration
@EnableAutoConfiguration
@Import({AppConfig.class})
public class MasterNodeApplication {
  public static void main(String[] args) {
    SpringApplication.run(MasterNodeApplication.class, args);
  }
}
