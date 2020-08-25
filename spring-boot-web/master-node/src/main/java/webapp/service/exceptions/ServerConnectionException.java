package webapp.service.exceptions;

public class ServerConnectionException extends RuntimeException {
  public ServerConnectionException(String message) {
    super(message);
  }
}
