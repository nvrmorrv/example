package webapp.service.operations;

import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RandomCodeIncreasingOperation implements CryptOperation {
  private final Map<String, String> resultsMap = new ConcurrentHashMap<>();
  private final Random random;

  private String increaseCodeRandomly(String string) {
    char[] characters = string.toCharArray();
    int num = random.nextInt(20) - 10;
    return IntStream.range(0, characters.length)
          .map(i -> ((int) characters[i]) + num)
          .mapToObj(Character::getName)
          .reduce("", String::concat);
  }

  @Override
  public String encrypt(String string) {
    String result = increaseCodeRandomly(string);
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
