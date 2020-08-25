package webapp.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.annotation.NewSpan;
import webapp.service.exceptions.ServerConnectionException;

@AllArgsConstructor
public class NodeClient {
  private final OkHttpClient client;

  @NewSpan
  public Response makeRequest(String uri) {
    try {
      Request request = new Request.Builder().url(uri).get().build();
      return client.newCall(request).execute();
    } catch (IOException exception) {
      throw new ServerConnectionException("Connection error, uri: " + uri);
    }
  }
}
