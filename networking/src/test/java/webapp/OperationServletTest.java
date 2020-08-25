package webapp;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class OperationServletTest {
  private final String path = "/webapp";
  private final int port = 8090;
  private final TestServer server = new TestServer(path, port);

  @Before
  public void serverStart() throws Exception {
    server.start();
  }

  @After
  public void serverStop() throws Exception {
    server.stop();
  }

  @Test
  public void shouldPassOnCorrectOperationRequest() throws IOException {
    String correctBody = "{\"operation\":\"concat\",\"payload\":{\"strings\":[\"a\",\"b\", \"c\"]}}";
    Response response = makeHttpRequest(true, correctBody);
    assertTrue(response.isSuccessful());
    assertEquals(200, response.code());
    assertEquals("{\"result\":\"abc\"}", response.body().string());
  }

  @Test
  public void shouldFailOnIncorrectRequestBody() throws IOException {
    String incorrectBody = "{\"payload\":{\"argA\": 20,\"argB\": 22}}";
    Response response = makeHttpRequest(true, incorrectBody);
    assertFalse(response.isSuccessful());
    assertEquals(400, response.code());
    assertEquals("{\"error\":\"wrong request format\"}" , response.body().string());
  }

  @Test
  public void shouldFailOnPassingUnknownOperation() throws IOException {
    String incorrectBody = "{\"operation\":\"unknown\",\"payload\":{\"strings\":[\"a\",\"b\", \"c\"]}}";
    Response response = makeHttpRequest(true, incorrectBody);
    assertFalse(response.isSuccessful());
    assertEquals(404, response.code());
    assertEquals("{\"error\":\"operation unknown is not found\"}" , response.body().string());
  }

  @Test
  public void shouldFailOnPassingIncorrectPayloadRequest() throws IOException {
    String incorrectBody = "{\"operation\":\"store\",\"payload\":{\"wrongField\":[\"a\",\"b\", \"c\"]}}";
    Response response = makeHttpRequest(false, incorrectBody);
    assertFalse(response.isSuccessful());
    assertEquals(400, response.code());
    assertEquals("{\"error\":\"wrong payload format\"}" , response.body().string());
  }

  private Response makeHttpRequest(boolean isPost, String body) throws IOException {
    OkHttpClient client = new OkHttpClient();
    MediaType type = MediaType.parse("application/json");
    String url = String.format("http://localhost:%1d%2s", port, path);
    Request request = (isPost) ?
      new Request.Builder()
        .post(RequestBody.create(type, body))
        .url(url).build() :
      new Request.Builder()
        .put(RequestBody.create(type, body))
        .url(url).build();
    return client.newCall(request).execute();
  }

  @AllArgsConstructor
  private static class TestServer {
    private final Server server = new Server();
    private final String path;
    private final int port;

    public void start() throws Exception {
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(port);
      server.setConnectors(new ServerConnector[]{connector});
      ServletHandler servletHandler = new ServletHandler();
      servletHandler.addServletWithMapping(OperationServlet.class, path);
      server.setHandler(servletHandler);
      server.start();
    }

    public void stop() throws Exception {
      server.stop();
    }
  }
}
