package webapp.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import webapp.service.dto.ErrorResponse;
import webapp.service.exceptions.BadRequestException;

public class MasterNodeControllerAdviceTest {
  @Test
  public void shouldPassOnGettingBadRequestExceptionInstance() {
    MasterNodeControllerAdvice controllerAdvice = new MasterNodeControllerAdvice();
    BadRequestException exception = new BadRequestException("bad request!");
    ErrorResponse response = controllerAdvice.responseBadRequestError(exception);
    assertEquals(exception.getMessage(), response.getError());
  }

  @Test
  public void shouldPassOnGettingExceptionInstance() {
    MasterNodeControllerAdvice controllerAdvice = new MasterNodeControllerAdvice();
    Exception exception = new BadRequestException("bad request!");
    ErrorResponse response = controllerAdvice.responseInternalServerError(exception);
    assertEquals("internal server error", response.getError());
  }
}
