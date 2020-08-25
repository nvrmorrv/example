package webapp.service.operations;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RandomLetterAddingOperation implements CryptOperation {
  private final Map<String, String> resultsMap = new ConcurrentHashMap<>();
  private final Random random;

  private String addRandomLatter(String string) {
    char latter = string.charAt(random.nextInt(string.length() - 1));
    return new StringBuilder(string)
          .insert(random.nextInt(string.length() - 1), latter)
          .toString();
  }

  @Override
  public String encrypt(String string) {
    String result = addRandomLatter(string);
    resultsMap.putIfAbsent(result, string);
    return result;
  }

  @Override
  public String decrypt(String string) {
    return Optional.ofNullable(resultsMap.get(string))
          .orElseThrow(() ->
                new EncryptionResultNotFoundException("Cannot decrypt: the results map doesnt contain passed key"));
  }

}
