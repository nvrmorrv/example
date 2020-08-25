package webapp.handlers;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import webapp.operation.ExtractOp;
import webapp.operation.OperationHandler;

@AllArgsConstructor
@Slf4j
public class ExtractHandler implements OperationHandler<ExtractOp, ExtractHandler.ExtractResult> {
  private final Map<Integer, Object> store;

  @Override
  public ExtractResult handle(ExtractOp operation) {
    log.debug("Extract. ID : {}", operation.getId());
    if (!store.containsKey(operation.getId())) {
      throw new IllegalArgumentException(
        String.format("passed id doesnt exist, id : %d", operation.getId()));
    } else {
      return new ExtractResult(store.get(operation.getId()));
    }
  }

  @AllArgsConstructor
  @Getter
  public static class ExtractResult {
    private final Object content;
  }
}
