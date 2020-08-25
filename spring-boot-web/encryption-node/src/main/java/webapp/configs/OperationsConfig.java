package webapp.configs;

import java.util.Random;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import webapp.service.operations.Base64FormatOperation;
import webapp.service.operations.CryptOperation;
import webapp.service.operations.RandomCodeIncreasingOperation;
import webapp.service.operations.RandomLetterAddingOperation;
import webapp.service.operations.ReverseOperation;

@Configuration
public class OperationsConfig {
  @Bean
  public CryptOperation base64FormatOperation() {
    return new Base64FormatOperation();
  }

  @Bean
  public CryptOperation randomCodeIncreasingOperation(Random random) {
    return new RandomCodeIncreasingOperation(random);
  }

  @Bean
  public CryptOperation randomLatterAddingOperation(Random random) {
    return new RandomLetterAddingOperation(random);
  }

  @Bean
  public CryptOperation reverseOperation() {
    return new ReverseOperation();
  }

}
