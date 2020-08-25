package webapp.operation;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ConcatOp {
  private final List<String> strings;
}
