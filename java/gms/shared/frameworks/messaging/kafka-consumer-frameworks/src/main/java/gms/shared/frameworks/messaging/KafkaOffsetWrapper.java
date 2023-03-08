package gms.shared.frameworks.messaging;

import com.google.auto.value.AutoValue;
import reactor.kafka.receiver.ReceiverOffset;

import java.util.function.Function;

/**
 * Convenience record class for storing a value from a kafka {@link reactor.kafka.receiver.ReceiverRecord}
 * with its {@link ReceiverOffset}. Can be used with collections/batch objects and a latest offset
 * to help enable batch processing and committing of kafka messages.
 */
@AutoValue
public abstract class KafkaOffsetWrapper<T> {

  public abstract ReceiverOffset getOffset();

  public abstract T getValue();

  public static <T> KafkaOffsetWrapper<T> create(ReceiverOffset offset, T value) {
    return new AutoValue_KafkaOffsetWrapper<>(offset, value);
  }

  /**
   * Convenience method to apply processing to the inner value of the offset wrapper
   *
   * @param valueMapper Function to map inner value to another type
   * @param <U> Type to transform to
   * @return A KafkaOffsetWrapper with a transformed value
   */
  public <U> KafkaOffsetWrapper<U> map(Function<T, U> valueMapper) {
    return KafkaOffsetWrapper.create(getOffset(), valueMapper.apply(getValue()));
  }

}