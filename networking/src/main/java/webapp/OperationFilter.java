package webapp;

import static webapp.JsonMapper.writeValueToJson;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;

@Slf4j
public class OperationFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("filter initialized");
  }

  @Override
  public void doFilter(ServletRequest servletRequest,
                       ServletResponse servletResponse,
                       FilterChain filterChain) throws IOException, ServletException {
    MDC.clear();
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    if (Collections.list(request.getHeaderNames()).contains("Request-ID")) {
      MDC.put("request-id", request.getHeader("Request-ID"));
      log.debug("Filter: request accepted");
      filterChain.doFilter(servletRequest, servletResponse);
    } else {
      HttpServletResponse response = (HttpServletResponse) servletResponse;
      response.setStatus(400);
      response.setHeader("Content-type", "application/json");
      response.getWriter()
        .write(writeValueToJson(new OperationServlet.ErrorResult("request doesnt have request-id header")));
      log.error("Filter. Request rejected: has not Request-ID header");
      log.error("Send response");
    }
  }

  @Override
  public void destroy() {
    log.info("filter destroyed");
  }
}
