package webapp.handlers;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import webapp.operation.ConcatOp;

public class ConcatHandlerTest {
  @Test
  public void shouldPassOnConcat(){
    ConcatHandler handler = new ConcatHandler();
    List<String> list = Arrays.asList("a","b","c");
    assertEquals("abc", handler.handle(new ConcatOp(list)).getResult());
  }
}
