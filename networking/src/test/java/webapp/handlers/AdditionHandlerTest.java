package webapp.handlers;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import webapp.operation.AdditionOp;

public class AdditionHandlerTest {

  @Test
  public void shouldPassOnAddition() {
    AdditionHandler handler = new AdditionHandler();
    assertEquals(25, (int) handler.handle(new AdditionOp(12, 13)).getResult());
  }
}
