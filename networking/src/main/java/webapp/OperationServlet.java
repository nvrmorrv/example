package webapp;

import static webapp.JsonMapper.getValueFromJson;
import static webapp.JsonMapper.writeValueToJson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import webapp.handlers.AdditionHandler;
import webapp.handlers.ConcatHandler;
import webapp.handlers.ExtractHandler;
import webapp.handlers.StoreHandler;
import webapp.handlers.WeatherHandler;
import webapp.operation.AdditionOp;
import webapp.operation.ConcatOp;
import webapp.operation.ExtractOp;
import webapp.operation.StoreOp;
import webapp.operation.WeatherOp;

@Slf4j
public class OperationServlet extends HttpServlet {
  private Map<String, Binder<JsonNode, ?, ?>> postMOperations;
  private Map<String, Binder<JsonNode, ?, ?>> putMOperations;
  private final Map<Integer, Object> store = new ConcurrentHashMap<>();


  @SneakyThrows
  @Override
  public void init() {
    try {
      log.debug("servlet init start");
      initPostMOperations();
      initPutMOperations();
      postMOperations.keySet().forEach((o) -> log.debug("added post operation: {}", o));
      putMOperations.keySet().forEach((o) -> log.debug("added put operation: {}", o));
      log.info("servlet initialized");
    } catch (Exception exception) {
      log.error("servlet init error: {} - {}", exception.getClass().getSimpleName(), exception.getMessage());
      throw new Exception(exception);
    }
  }

  private void initPostMOperations() throws IOException {
    postMOperations = ImmutableMap.of(
      "addition", new Binder<>(view -> getOperation(view, AdditionOp.class), new AdditionHandler()),
      "concat", new Binder<>(view -> getOperation(view, ConcatOp.class), new ConcatHandler()),
      "extract", new Binder<>(view -> getOperation(view, ExtractOp.class), new ExtractHandler(store)),
      "weather", new Binder<>(view -> getOperation(view, WeatherOp.class), new WeatherHandler(getWeatherApi())));
  }

  private void initPutMOperations() {
    putMOperations = ImmutableMap.of(
      "store", new Binder<>((view) -> getOperation(view, StoreOp.class), new StoreHandler(store)));
  }

  private <T> T getOperation(JsonNode view, Class<T> cl) {
    try {
      return getValueFromJson(view, cl);
    } catch (IOException ex) {
      throw new IllegalStateException("wrong payload format", ex);
    }
  }

  private WeatherHandler.WeatherApi getWeatherApi() throws IOException {
    String filename = "weatherApi.yaml";
    log.debug("Getting weather api file. File name : {}", filename);
    String path = getClass().getClassLoader().getResource(filename).getPath();
    log.debug("Path : {}", path);
    return new ObjectMapper(new YAMLFactory()).readValue(new File(path), WeatherHandler.WeatherApi.class);
  }

  @Override
  public void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.debug("request method: PUT");
    doRequest(request, response, putMOperations);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    log.debug("request method: POST");
    doRequest(request, response, postMOperations);
  }

  public void doRequest(HttpServletRequest request,
                        HttpServletResponse response,
                        Map<String, Binder<JsonNode, ?, ?>> operations) throws IOException {
    try {
      RequestDesc requestDesc = getRequestDesc(request);
      log.info("operation: {}", requestDesc.operation);
      Binder<JsonNode, ?, ?> binder = getBinder(requestDesc, operations);
      Object result = binder.apply(requestDesc.payload);
      log.info("operation {} performed", requestDesc.operation);
      setResponse(response, 200, result);
    } catch (IOException | IllegalStateException ex) {
      log.error("{} : {}", ex.getClass().getSimpleName(), ex.getMessage());
      setResponse(response, 400, new ErrorResult(ex.getMessage()));
    } catch (IllegalArgumentException ex) {
      log.error("{} : {}", ex.getClass().getSimpleName(), ex.getMessage());
      setResponse(response, 404, new ErrorResult(ex.getMessage()));
    } catch (Exception ex) {
      log.error("{} : {}", ex.getClass().getSimpleName(), ex.getMessage());
      setResponse(response, 500, new ErrorResult(ex.getMessage()));
    }
  }

  private void setResponse(HttpServletResponse response,
                           int status,
                           Object result) throws IOException {
    response.getWriter().write(writeValueToJson(result));
    response.setHeader("Content-type", "application/json");
    response.setStatus(status);
    log.debug("send response");
  }

  private RequestDesc getRequestDesc(HttpServletRequest request) throws IOException {
    try {
      return getValueFromJson(request.getInputStream(), RequestDesc.class);
    } catch (IOException ex) {
      throw new IOException("wrong request format", ex);
    }
  }

  private Binder<JsonNode, ?, ?> getBinder(RequestDesc requestDesc,
                                           Map<String, Binder<JsonNode, ?, ?>> operations) {
    if (operations.containsKey(requestDesc.getOperation())) {
      return operations.get(requestDesc.getOperation());
    } else {
      throw new IllegalArgumentException(
       String.format("operation %1s is not found", requestDesc.getOperation()));
    }
  }

  @AllArgsConstructor
  public static class ErrorResult {
    private final String error;
  }

  @AllArgsConstructor
  @Getter
  public static class RequestDesc {
    @NonNull
    private final String operation;
    @NonNull
    private final JsonNode payload;
  }
}
