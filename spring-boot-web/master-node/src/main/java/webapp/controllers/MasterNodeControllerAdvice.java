package webapp.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import webapp.service.dto.ErrorResponse;
import webapp.service.exceptions.BadRequestException;

@ControllerAdvice
@Slf4j
public class MasterNodeControllerAdvice {
  @ResponseBody
  @ExceptionHandler({BadRequestException.class,})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse responseBadRequestError(BadRequestException exception) {
    log.debug("BAD_REQUEST: exception : {}, message: {}",
          exception.getClass().getSimpleName(),
          exception.getMessage());
    return new ErrorResponse(exception.getMessage());
  }

  @ResponseBody
  @ExceptionHandler({Exception.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorResponse responseInternalServerError(Exception exception) {
    log.info("INTERNAL_SERVER_ERROR: exception : {}, message: {}",
          exception.getClass().getSimpleName(),
          exception.getMessage());
    return new ErrorResponse("internal server error");
  }

}
