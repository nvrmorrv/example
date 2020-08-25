package webapp.service.operations;

import java.util.Base64;

public class Base64FormatOperation implements CryptOperation {
  @Override
  public String encrypt(String string) {
    return Base64.getEncoder().encodeToString(string.getBytes());
  }

  @Override
  public String decrypt(String string) {
    return new String(Base64.getDecoder().decode(string.getBytes()));
  }
}
