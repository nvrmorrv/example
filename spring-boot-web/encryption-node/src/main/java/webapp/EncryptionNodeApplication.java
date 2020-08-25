package webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import webapp.configs.OperationsConfig;
import webapp.configs.PropsConfig;
import webapp.configs.ServiceConfig;

@Configuration
@EnableAutoConfiguration
@Import({ServiceConfig.class, OperationsConfig.class, PropsConfig.class})
public class EncryptionNodeApplication {
  public static void main(String[] args) {
    SpringApplication.run(EncryptionNodeApplication.class, args);
  }
}
