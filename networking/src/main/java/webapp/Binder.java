package webapp;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import webapp.operation.OperationHandler;

@AllArgsConstructor
public class Binder<V, O, R> {
  private final Function<V,O> mapper;
  private final OperationHandler<O, R> handler;

  public R apply(V view) throws Exception {
    return handler.handle(mapper.apply(view));
  }
}
