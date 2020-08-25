package webapp.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static webapp.HelpMethods.getNewResponse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.Response;
import java.util.Optional;
import org.junit.Test;
import webapp.service.dto.DecryptionErrorResult;
import webapp.service.dto.DecryptionResult;
import webapp.service.dto.EncryptionResult;
import webapp.service.dto.MonitorResult;
import webapp.service.exceptions.BadRequestException;
import webapp.service.exceptions.InternalErrorException;
import webapp.service.exceptions.WrongContextPathException;

public class NodeResponseHandlerTest {

  @Test
  public void shouldPassOnHandlingEncryptionResponseWithCode200() throws JsonProcessingException {
    NodeResponseHandler handler = new NodeResponseHandler();
    EncryptionResult result = new EncryptionResult("string");
    String resStr = new ObjectMapper().writeValueAsString(result);
    String url = "http://some";
    Response response = getNewResponse(200, url, Optional.of(resStr));
    assertEquals(result.getResult(), handler.handleEncResponse(response, url).getResult());
  }


  @Test
  public void shouldPassOnHandlingMonitorResponseWithCode200() throws JsonProcessingException {
    NodeResponseHandler handler = new NodeResponseHandler();
    MonitorResult result = new MonitorResult("yes");
    String resStr = new ObjectMapper().writeValueAsString(result);
    String url = "http://some";
    Response response = getNewResponse(200, url, Optional.of(resStr));
    assertEquals(result.getStatus(), handler.handleMonitorResponse(response, url).getStatus());
  }

  @Test
  public void shouldPassOnHandlingDecryptionResponseWithCode200() throws JsonProcessingException {
    NodeResponseHandler handler = new NodeResponseHandler();
    DecryptionResult result = new DecryptionResult("string");
    String resStr = new ObjectMapper().writeValueAsString(result);
    String url = "http://some";
    Response response = getNewResponse(200, url, Optional.of(resStr));
    assertEquals(result.getResult(), handler.handleDecResponse(response, url).getResult());
  }

  @Test
  public void shouldPassOnHandlingDecryptionResponseWithCode400() throws JsonProcessingException {
    NodeResponseHandler handler = new NodeResponseHandler();
    DecryptionErrorResult result = new DecryptionErrorResult("bad request");
    String resStr = new ObjectMapper().writeValueAsString(result);
    String url = "http://some";
    Response response = getNewResponse(400, url, Optional.of(resStr));
    assertThatThrownBy(() -> handler.handleDecResponse(response, url))
          .isInstanceOf(BadRequestException.class)
          .hasMessage(String.format("Bad request, message: %1s , uri: %2s",
                result.getError(), url));
    ;
  }

  @Test
  public void shouldFailOnHandlingResponseWithCode404() {
    NodeResponseHandler handler = new NodeResponseHandler();
    String url = "http://some";
    Response response = getNewResponse(404, url, Optional.empty());
    assertThatThrownBy(() -> handler.handleMonitorResponse(response, url))
          .isInstanceOf(WrongContextPathException.class)
          .hasMessage("Wrong context path, uri: " + url);
    assertThatThrownBy(() -> handler.handleEncResponse(response, url))
          .isInstanceOf(WrongContextPathException.class)
          .hasMessage("Wrong context path, uri: " + url);
  }

  @Test
  public void shouldFailOnHandlingResponseWithAnyRestCodes() {
    NodeResponseHandler handler = new NodeResponseHandler();
    String url = "http://some";
    Response response = getNewResponse(500, url, Optional.empty());
    assertThatThrownBy(() -> handler.handleMonitorResponse(response, url))
          .isInstanceOf(InternalErrorException.class)
          .hasMessage(String.format("Internal error of node server, code: %1s, uri: %2s", 500, url));
    assertThatThrownBy(() -> handler.handleEncResponse(response, url))
          .isInstanceOf(InternalErrorException.class)
          .hasMessage(String.format("Internal error of node server, code: %1s, uri: %2s", 500, url));
  }
}
