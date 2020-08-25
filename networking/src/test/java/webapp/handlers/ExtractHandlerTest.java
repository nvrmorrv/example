package webapp.handlers;

import static org.junit.Assert.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import webapp.operation.ExtractOp;

public class ExtractHandlerTest {
  private final Map<Integer,Object> store = new HashMap<>();

  @Test
  public void shouldPassWhenIdExists() {
    store.put(12, "value12");
    ExtractHandler handler = new ExtractHandler(store);
    assertEquals("value12", handler.handle(new ExtractOp(12)).getContent());
  }

  @Test
  public void shouldFailWhenIdDoesntExists() {
    ExtractHandler handler = new ExtractHandler(store);
    assertThatThrownBy(() -> handler.handle(new ExtractOp(12)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessage("passed id doesnt exist, id : 12");
  }
}
