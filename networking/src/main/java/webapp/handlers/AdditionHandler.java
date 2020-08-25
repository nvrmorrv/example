package webapp.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import webapp.operation.AdditionOp;
import webapp.operation.OperationHandler;

@Slf4j
public class AdditionHandler implements OperationHandler<AdditionOp, AdditionHandler.AdditionResult> {

  private Integer add(AdditionOp operation) {
    Integer result = operation.getArgA() + operation.getArgB();
    log.debug("Addition. argA : {}, argB : {}, result : {}",
        operation.getArgA(), operation.getArgB(), result);
    return result;
  }

  @Override
  public AdditionResult handle(AdditionOp operation) {
    return new AdditionResult(add(operation));
  }

  @AllArgsConstructor
  @Getter
  public static class AdditionResult {
    private final Integer result;
  }
}
