package webapp;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Random;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import webapp.controllers.EncryptionNodeController;
import webapp.controllers.EncryptionNodeControllerAdvice;
import webapp.service.operations.CryptOperation;
import webapp.service.operations.ReverseOperation;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = EncryptionNodeTest.TestConfig.class)
@AutoConfigureMockMvc
public class EncryptionNodeTest {

  @Autowired
  MockMvc mockMvc;

  @Autowired
  ObjectMapper mapper;

  @Test
  public void shouldPassOnEncryption() throws Exception {
    String string = "656ax324sx";
    String resStr = new ReverseOperation().encrypt(string);
    String resContent = mapper.writeValueAsString(
          new EncryptionNodeController.ResponseFormat(resStr));
    mockMvc.perform(get("/encrypt/" + string).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().string(resContent));
  }

  @Test
  public void shouldPassOnDecryption() throws Exception {
    String string = "656ax324sx";
    String resStr = new ReverseOperation().encrypt(string);
    String resContent = mapper.writeValueAsString(
          new EncryptionNodeController.ResponseFormat(resStr));
    mockMvc.perform(get("/decrypt/" + string).contentType(MediaType.APPLICATION_JSON))
          .andExpect(status().isOk())
          .andExpect(content().string(resContent));
  }

  @Configuration
  @EnableAutoConfiguration
  public static class TestConfig {
    @Bean
    public EncryptionNodeController encryptionNodeController(
          CryptOperation reverseOperation,
          Random random) {
      return new EncryptionNodeController(reverseOperation, random, new SimpleMeterRegistry());
    }

    @Bean
    public EncryptionNodeControllerAdvice encryptionNodeControllerAdvice() {
      return new EncryptionNodeControllerAdvice();
    }

    @Bean
    public CryptOperation reverseOperation() {
      return new ReverseOperation();
    }

    @Bean
    public Random random() {
      return new Random(System.currentTimeMillis());
    }
  }

}
