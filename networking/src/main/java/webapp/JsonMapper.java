package webapp;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import lombok.Getter;


public class JsonMapper {
  @Getter
  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  public static <T> T getValueFromJson(JsonNode node, Class<T> cl) throws IOException {
    return mapper.treeToValue(node, cl);
  }

  public static <T> T getValueFromJson(InputStream stream, Class<T> cl) throws IOException {
    return mapper.readValue(stream, cl);
  }

  public static String writeValueToJson(Object object) throws JsonProcessingException {
    return mapper.writeValueAsString(object);
  }


}
