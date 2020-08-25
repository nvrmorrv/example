package webapp.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;


@AllArgsConstructor
@Getter
public class AdditionOp {
  @NonNull
  private final Integer argA;
  @NonNull
  private final Integer argB;
}
