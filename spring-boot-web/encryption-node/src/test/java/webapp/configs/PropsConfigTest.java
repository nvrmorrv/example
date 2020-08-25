package webapp.configs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PropsConfig.class)
public class PropsConfigTest {

  @Test
  public void shouldUpWithoutWiringErrors() {
  }

}
