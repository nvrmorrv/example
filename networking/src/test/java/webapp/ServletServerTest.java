package webapp;

import static org.junit.Assert.assertTrue;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.junit.Test;
import org.mockito.Mockito;

public class ServletServerTest {
  private final String path = "/webapp";

  @Test
  public void shouldPassOnRequest() throws Exception {
    ServletServer server = new ServletServer();
    server.setServlet(Mockito.mock(GenericServlet.class).getClass(), path);
    server.setFilter(TestFilter.class, path);
    server.start();
    assertTrue(makeHttpRequest());
    server.shutdown();
  }

  private boolean makeHttpRequest() throws IOException {
    OkHttpClient client = new OkHttpClient();
    int port = ServletServer.DEFAULT_PORT;
    Request request = new Request.Builder()
        .url(String.format("http://localhost:%1d%2s", port, path))
        .build();
    return client.newCall(request).execute().isSuccessful();
  }

  public static class TestFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException { }

    @Override
    public void destroy() { }

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }
}
