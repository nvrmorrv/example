package webapp.controllers;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import webapp.service.operations.EncryptionResultNotFoundException;

@ControllerAdvice
@Slf4j
public class EncryptionNodeControllerAdvice {
  @ResponseBody
  @ExceptionHandler(EncryptionResultNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorResponse responseBadRequest(EncryptionResultNotFoundException ex) {
    log.info("BAD REQUEST. Exception : {}, message : {}", ex.getClass().getSimpleName(), ex.getMessage());
    return new ErrorResponse(ex.getMessage());
  }

  @AllArgsConstructor
  @Getter
  static public class ErrorResponse {
    private final String error;
  }
}
