package webapp.configs;

import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
      OperationsConfig.class,
      OperationsConfigTest.RandomConfig.class})
public class OperationsConfigTest {
  @Test
  public void shouldUpWithoutWiringErrors() {
  }

  @Configuration
  public static class RandomConfig {
    @Bean
    public Random random() {
      return new Random();
    }
  }
}
