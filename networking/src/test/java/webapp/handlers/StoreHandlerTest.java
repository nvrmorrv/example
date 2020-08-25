package webapp.handlers;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import webapp.operation.StoreOp;

public class StoreHandlerTest {

  @Test
  public void shouldPassOnStoringContent() {
    Map<Integer,Object> store = new HashMap<>();
    StoreHandler handler = new StoreHandler(store);
    assertEquals("data stored under id : 12",
      handler.handle(new StoreOp(12, "value")).getResult());
    assertEquals("data stored under id : 12",
      handler.handle(new StoreOp(12, "value12")).getResult());
  }

}
