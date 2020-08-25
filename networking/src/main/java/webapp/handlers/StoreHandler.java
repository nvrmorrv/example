package webapp.handlers;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import webapp.operation.OperationHandler;
import webapp.operation.StoreOp;

@AllArgsConstructor
@Slf4j
public class StoreHandler implements OperationHandler<StoreOp, StoreHandler.StoreResult> {
  private final Map<Integer, Object> store;

  @Override
  public StoreResult handle(StoreOp operation) {
    log.debug("Store. ID : {}", operation.getId());
    if (store.containsKey(operation.getId())) {
      store.replace(operation.getId(), operation.getContent());
    } else {
      store.put(operation.getId(), operation.getContent());
    }
    log.debug("Store : data stored");
    return new StoreResult(String.format("data stored under id : %d",operation.getId()));
  }

  @AllArgsConstructor
  @Getter
  public static class StoreResult {
    private final String result;
  }
}
