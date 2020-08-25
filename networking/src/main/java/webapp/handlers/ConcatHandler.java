package webapp.handlers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import webapp.operation.ConcatOp;
import webapp.operation.OperationHandler;

@Slf4j
public class ConcatHandler implements OperationHandler<ConcatOp, ConcatHandler.ConcatResult> {
  @Override
  public ConcatResult handle(ConcatOp operation) {
    return new ConcatResult(concat(operation));
  }

  private String concat(ConcatOp op) {
    log.debug("Concat. strings: {}", op.getStrings()
        .stream()
        .map((s) -> s + " ")
        .reduce("", String::concat));
    String result = op.getStrings().stream().reduce("", String::concat);
    log.debug("concat result: {}", result);
    return result;
  }

  @AllArgsConstructor
  @Getter
  public static class ConcatResult {
    private final String result;
  }
}
