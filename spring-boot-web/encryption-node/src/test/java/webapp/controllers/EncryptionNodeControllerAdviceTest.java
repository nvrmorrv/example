package webapp.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import webapp.service.operations.EncryptionResultNotFoundException;

public class EncryptionNodeControllerAdviceTest {

  @Test
  public void shouldReturnBadRequest() {
    EncryptionNodeControllerAdvice controllerAdvice =
          new EncryptionNodeControllerAdvice();
    EncryptionResultNotFoundException exception
          = new EncryptionResultNotFoundException("bad request");
    assertEquals(exception.getMessage(),
          controllerAdvice.responseBadRequest(exception).getError());
  }


}
