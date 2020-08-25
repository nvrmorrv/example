package webapp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;
import lombok.SneakyThrows;
import webapp.service.dto.DecryptionErrorResult;
import webapp.service.dto.DecryptionResult;
import webapp.service.dto.EncryptionResult;
import webapp.service.dto.MonitorResult;
import webapp.service.exceptions.BadRequestException;
import webapp.service.exceptions.InternalErrorException;
import webapp.service.exceptions.WrongContextPathException;

public class NodeResponseHandler {
  private final ObjectMapper mapper = new ObjectMapper();

  @SneakyThrows
  public EncryptionResult handleEncResponse(Response response, String uri) {
    int code = response.code();
    handleResponseByDefault(code, uri);
    return mapper.readValue(response.body().bytes(), EncryptionResult.class);
  }

  @SneakyThrows
  public DecryptionResult handleDecResponse(Response response, String uri) {
    int code = response.code();
    if (code == 400) {
      String em = mapper.readValue(response.body().bytes(), DecryptionErrorResult.class).getError();
      throw new BadRequestException(
            String.format("Bad request, message: %1s , uri: %2s", em, uri));
    }
    handleResponseByDefault(code, uri);
    return mapper.readValue(response.body().bytes(), DecryptionResult.class);
  }

  @SneakyThrows
  public MonitorResult handleMonitorResponse(Response response, String uri) {
    int code = response.code();
    handleResponseByDefault(code, uri);
    return mapper.readValue(response.body().bytes(), MonitorResult.class);
  }

  private void handleResponseByDefault(int code, String uri) {
    if (code == 404) {
      throw new WrongContextPathException("Wrong context path, uri: " + uri);
    } else if (code != 200) {
      throw new InternalErrorException(
            String.format("Internal error of node server, code: %1s, uri: %2s", code, uri));
    }
  }
}
