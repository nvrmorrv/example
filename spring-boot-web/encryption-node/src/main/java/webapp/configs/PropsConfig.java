package webapp.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import webapp.service.MasterNodeUri;

@Configuration
public class PropsConfig {
  @ConfigurationProperties(prefix = "master-node")
  @Bean
  public MasterNodeUri masterNodeUri() {
    return new MasterNodeUri();
  }
}
