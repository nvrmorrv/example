package webapp.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Random;
import org.junit.Test;
import webapp.service.operations.CryptOperation;

public class EncryptionNodeControllerTest {
  private final String INIT_STR = "init string";
  private final String RES_STR = "res string";
  private final Random random = new Random(0L);

  CryptOperation operation = new CryptOperation() {
    @Override
    public String encrypt(String string) {
      return RES_STR;
    }

    @Override
    public String decrypt(String string) {
      return RES_STR;
    }
  };

  @Test
  public void shouldPassOnEncryption() {
    EncryptionNodeController controller =
          new EncryptionNodeController(operation, random, new SimpleMeterRegistry());
    assertEquals(RES_STR, controller.encrypt(INIT_STR).getResult());
  }

  @Test
  public void shouldPassOnDecryption() {
    EncryptionNodeController controller =
          new EncryptionNodeController(operation, random, new SimpleMeterRegistry());
    assertEquals(RES_STR, controller.decrypt(INIT_STR).getResult());
  }

}
