package webapp.service.exceptions;

public class WrongContextPathException extends RuntimeException {
  public WrongContextPathException(String message) {
    super(message);
  }
}
