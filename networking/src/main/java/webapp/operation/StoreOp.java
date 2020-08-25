package webapp.operation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@AllArgsConstructor
@Getter
public class StoreOp {
  @NonNull
  private final Integer id;
  @NonNull
  private final Object content;
}
