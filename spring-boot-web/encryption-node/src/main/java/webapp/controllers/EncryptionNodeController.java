package webapp.controllers;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.Random;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import webapp.service.operations.CryptOperation;

@RestController
@AllArgsConstructor
@Slf4j
public class EncryptionNodeController {
  private final CryptOperation operation;
  private final Random random;
  private final MeterRegistry meterRegistry;

  @GetMapping(path = "/encrypt/{string}")
  @ResponseBody
  @NewSpan
  public ResponseFormat encrypt(@PathVariable(name = "string") String string) {
    Timer.Sample sample = Timer.start(meterRegistry);
    log.debug("Encrypt operation, operation : {}", operation.getClass().getSimpleName());
    delayHandling();
    String result = operation.encrypt(string);
    log.debug("Encryption is done");
    sample.stop(meterRegistry.timer("encryption-node.encryptionTime"));
    return new ResponseFormat(result);
  }

  @GetMapping(path = "/decrypt/{string}")
  @ResponseBody
  @NewSpan
  public ResponseFormat decrypt(@PathVariable(name = "string") String string) {
    Timer.Sample sample = Timer.start(meterRegistry);
    log.debug("Decrypt operation, operation : {}", operation.getClass().getSimpleName());
    delayHandling();
    String result = operation.decrypt(string);
    log.debug("Decryption is done");
    sample.stop(meterRegistry.timer("encryption-node.decryptionTime"));
    return new ResponseFormat(result);
  }

  @SneakyThrows
  private void delayHandling() {
    Thread.sleep(random.nextInt(3000));
  }


  @AllArgsConstructor
  @Getter
  public static class ResponseFormat {
    private final String result;
  }
}
