package webapp.configs;

import okhttp3.OkHttpClient;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import webapp.EncryptionNodeAppStartListener;
import webapp.controllers.EncryptionNodeController;
import webapp.controllers.EncryptionNodeControllerAdvice;
import webapp.service.EncryptionNodeInfo;
import webapp.service.MasterNodeUri;
import webapp.service.operations.CryptOperation;

@Configuration
@EnableRetry
@Slf4j
public class ServiceConfig {

  @Bean
  public EncryptionNodeController encryptionNodeController(List<CryptOperation> operationList,
                                                           Random random,
                                                           PrometheusMeterRegistry registry) {
    Collections.shuffle(operationList, random);
    CryptOperation operation = operationList.get(0);
    log.info("Operation :  {}", operation.getClass().getName());
    return new EncryptionNodeController(operation, random, registry);
  }

  @Bean
  public EncryptionNodeControllerAdvice encryptionNodeControllerAdvice() {
    return new EncryptionNodeControllerAdvice();
  }

  @Bean
  public EncryptionNodeAppStartListener encryptionNodeAppStartListener(MasterNodeUri mUri,
                                                                       EncryptionNodeInfo enData,
                                                                       OkHttpClient client) {
    if (mUri.getNotifyUri() == null) {
      throw new IllegalArgumentException("Master node uri property is not specified");
    }
    return new EncryptionNodeAppStartListener(mUri, enData, client);
  }

  @Bean
  public Random random() {
    return new Random(System.currentTimeMillis());
  }

  @Bean
  public EncryptionNodeInfo encryptionNodeUri(@Value("${server.port}") int port,
                                              @Value("${server.address:localhost}") String address,
                                              @Value("${server.servlet.context-path}") String contextPath,
                                              @Value("${management.endpoints.web.base-path}") String monitorPath) {
    return new EncryptionNodeInfo(port, address, contextPath, monitorPath);
  }

  @Bean
  public OkHttpClient okHttpClient() {
    return new OkHttpClient();
  }
}
