package webapp;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.MongoDBContainer;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MasterNodeApplication.class)
public class AppConfigTest {
  @ClassRule
  public static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.0");

  @DynamicPropertySource
  public static void dynamicPropertySource(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.port", mongoDBContainer::getFirstMappedPort);
    registry.add("spring.data.mongodb.host", mongoDBContainer::getHost);
  }

  @Test
  public void contextShouldBeUpWithoutWiringErrors() {
  }
}
