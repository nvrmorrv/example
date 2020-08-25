package webapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.io.IOException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import webapp.service.EncryptionNodeInfo;
import webapp.service.MasterNodeUri;

@AllArgsConstructor
@Slf4j
public class EncryptionNodeAppStartListener implements ApplicationListener<ContextRefreshedEvent> {
  private final MasterNodeUri mNodeUri;
  private final EncryptionNodeInfo nodeUri;
  private final OkHttpClient client;
  private final ObjectMapper mapper = new ObjectMapper();


  @Override
  @Retryable(
        value = {ConnectionException.class},
        maxAttempts = 30,
        backoff = @Backoff(delay = 3000, multiplier = 1.61)
  )
  public void onApplicationEvent(ContextRefreshedEvent event) {
    EncryptionNodePaths paths = getNodePaths();
    notifyMasterNode(paths);
  }


  public void notifyMasterNode(EncryptionNodePaths info) {
    try {
      log.info("Trying to connect to master node, url : {} ", mNodeUri.getNotifyUri());
      Request request = new Request.Builder()
            .url(mNodeUri.getNotifyUri())
            .post(RequestBody
                  .create(MediaType.parse("application/json"), mapper.writeValueAsString(info)))
            .build();
      Response response = client.newCall(request).execute();
      if (!response.isSuccessful()) {
        throw new IllegalArgumentException(
              String.format("Notifying master node error, uri: %s ", mNodeUri.getNotifyUri()));
      }
    } catch (IOException exception) {
      throw new ConnectionException(exception.getMessage());
    }
  }

  private EncryptionNodePaths getNodePaths() {
    String ePath = String.format("http://%1s:%2d%3s/encrypt",
          nodeUri.getAddress(),
          nodeUri.getPort(),
          nodeUri.getContextPath()
    );
    String dPath = String.format("http://%1s:%2d%3s/decrypt",
          nodeUri.getAddress(),
          nodeUri.getPort(),
          nodeUri.getContextPath()
    );
    String mPath = String.format("http://%1s:%2d%3s%4s/health",
          nodeUri.getAddress(),
          nodeUri.getPort(),
          nodeUri.getContextPath(),
          nodeUri.getMonitorPath()
    );
    return new EncryptionNodePaths(ePath, dPath, mPath);
  }

  @AllArgsConstructor
  @Getter
  private static class EncryptionNodePaths {
    private final String encryptionPath;
    private final String decryptionPath;
    private final String monitorPath;
  }

  public static class ConnectionException extends RuntimeException {
    public ConnectionException(String message) {
      super(message);
    }
  }
}
