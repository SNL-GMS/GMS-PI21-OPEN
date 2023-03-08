package gms.dataacquisition.stationreceiver.cd11.dataman;

import org.apache.kafka.clients.producer.RecordMetadata;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

public class TestResult<T> implements SenderResult<T> {
  private final Exception exception;
  private final T correlationMetadata;

  public TestResult(Exception exception, T correlationMetadata) {
    this.exception = exception;
    this.correlationMetadata = correlationMetadata;
  }

  public static <T> SenderResult<T> success(SenderRecord<?, ?, T> record) {
    return success(record.correlationMetadata());
  }

  public static <T> TestResult<T> success(T correlationMetadata) {
    return new TestResult<>(null, correlationMetadata);
  }

  public static <T> SenderResult<T> error(SenderRecord<?, ?, T> record, Exception exception) {
    return error(exception, record.correlationMetadata());
  }

  public static <T> TestResult<T> error(Exception exception, T correlationMetadata) {
    return new TestResult<>(exception, correlationMetadata);
  }

  @Override
  public RecordMetadata recordMetadata() {
    return null;
  }

  @Override
  public Exception exception() {
    return exception;
  }

  @Override
  public T correlationMetadata() {
    return correlationMetadata;
  }

  @Override
  public String toString() {
    return "TestResult{" +
      "exception=" + exception +
      ", correlationMetadata=" + correlationMetadata +
      '}';
  }
}
