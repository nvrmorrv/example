package webapp.operation;

public interface OperationHandler<T, R> {
  R handle(T operation) throws Exception;
}
