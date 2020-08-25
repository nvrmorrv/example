package webapp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.util.EnumSet;
import javax.servlet.DispatcherType;
import javax.servlet.GenericServlet;
import lombok.AllArgsConstructor;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class OperationFilterTest {
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
  public void shouldPassWhenRequestHasRequestIdHeader() throws Exception {
    assertTrue(makeHttpRequest(true).isSuccessful());
  }

  @Test
  public void shouldFailWhenRequestHasNotRequestIdHeader() throws Exception {
    Response response = makeHttpRequest(false);
    assertFalse(response.isSuccessful());
    assertEquals(400, response.code());
    assertEquals("{\"error\":\"request doesnt have request-id header\"}" , response.body().string());
  }

  private Response makeHttpRequest(boolean makeWithHeader) throws IOException {
    OkHttpClient client = new OkHttpClient();
    Request request = (makeWithHeader) ?
      new Request.Builder()
      .addHeader("Request-ID", "122")
      .url(String.format("http://localhost:%1d%2s", port, path))
      .build() :
      new Request.Builder()
      .url(String.format("http://localhost:%1d%2s", port, path))
      .build();
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
      servletHandler.addServletWithMapping(Mockito.mock(GenericServlet.class).getClass(), path);
      servletHandler.addFilterWithMapping(OperationFilter.class, path, EnumSet.of(DispatcherType.REQUEST));
      server.setHandler(servletHandler);
      server.start();
    }

    public void stop() throws Exception {
      server.stop();
    }
  }
}
