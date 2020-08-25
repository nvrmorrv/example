package webapp.service.operations;

public interface CryptOperation {
  String encrypt(String string);

  String decrypt(String string);
}
