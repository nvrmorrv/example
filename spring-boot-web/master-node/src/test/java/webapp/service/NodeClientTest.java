package webapp.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static webapp.HelpMethods.getNewResponse;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import webapp.service.exceptions.ServerConnectionException;

@RunWith(MockitoJUnitRunner.class)
public class NodeClientTest {

  @Mock
  OkHttpClient client;

  @Test
  public void shouldPassOnCorrectRequest() throws IOException {
    NodeClient nodeClient = new NodeClient(client);
    Call call = Mockito.mock(Call.class);
    String url = "http://some";
    Response response = getNewResponse(200, url, Optional.empty());
    Mockito.when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
    Mockito.when(call.execute()).thenReturn(response);
    assertEquals(response, nodeClient.makeRequest(url));
  }


  @Test
  public void shouldFailOnUnsuccessfulRequest() throws IOException {
    NodeClient nodeClient = new NodeClient(client);
    Call call = Mockito.mock(Call.class);
    String url = "http://some";
    Mockito.when(client.newCall(Mockito.any(Request.class))).thenReturn(call);
    Mockito.when(call.execute()).thenThrow(new IOException("error message"));
    assertThatThrownBy(() -> nodeClient.makeRequest(url))
          .isInstanceOf(ServerConnectionException.class)
          .hasMessage("Connection error, uri: " + url);
  }
}
