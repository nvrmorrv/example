package webapp.service.operations;

public class ReverseOperation implements CryptOperation {
  @Override
  public String encrypt(String str) {
    return reverse(str);
  }

  @Override
  public String decrypt(String str) {
    return reverse(str);
  }

  private String reverse(String str) {
    return new StringBuilder(str).reverse().toString();
  }
}
