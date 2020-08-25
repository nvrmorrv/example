package webapp;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;

@FieldDefaults(makeFinal = true)
@Slf4j
public class ServletServer {
  public static final int DEFAULT_PORT = 50000;
  private Server server;
  private final Set<ImmutablePair<Class<? extends Servlet>,String>> servlets = new HashSet<>();
  private final Set<ImmutablePair<Class<? extends Filter>,String>> filters = new HashSet<>();


  public ServletServer() {
    server = new Server();
    log.info("server created");
  }

  public void setServlet(Class<? extends Servlet> servletCl, String path) {
    servlets.add(new ImmutablePair<>(servletCl, path));
  }

  public void setFilter(Class<? extends Filter> filterCl, String path) {
    filters.add(new ImmutablePair<>(filterCl, path));
  }


  public void start() throws Exception {
    start(DEFAULT_PORT);
  }

  public void start(int port) throws Exception {
    log.debug("server start");
    ServerConnector connector = new ServerConnector(server);
    connector.setPort(port);
    server.setConnectors(new Connector[]{connector});
    ServletHandler handler = new ServletHandler();
    addServlets(handler);
    addFilters(handler);
    server.setHandler(handler);
    server.start();
    log.debug("server assigns port: {}", port);
    log.info("server is started");
  }

  private void addServlets(ServletHandler handler) {
    servlets.forEach(p -> {
      handler.addServletWithMapping(p.getKey(), p.getValue());
      log.debug("handler {} adds servlet {} to path {}",
          handler, p.getKey().getCanonicalName(), p.getValue());
    });
  }

  private void addFilters(ServletHandler handler) {
    filters.forEach(p -> {
      handler.addFilterWithMapping(p.getKey(), p.getValue(), EnumSet.of(DispatcherType.REQUEST));
      log.debug("handler {} adds filter {} on path {}",
          handler, p.getKey().getCanonicalName(), p.getValue());
    });
  }

  public void join() throws InterruptedException {
    server.join();
    log.info("server is closed");
  }

  public void shutdown() {
    try {
      server.stop();
      log.info("server is stopped");
    } catch (Exception exception) {
      log.info(exception.getMessage());
    }
  }

  public static void main(String... args) throws Exception {
    ServletServer server = new ServletServer();
    server.setServlet(OperationServlet.class, "/operation");
    server.setFilter(OperationFilter.class, "/operation");
    server.start(8090);
  }

}
