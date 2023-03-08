package gms.shared.frameworks.injector;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import java.util.function.UnaryOperator;

public interface Modifier<T extends Iterable> extends UnaryOperator<T> {
  /**
   * Applies this function to the given argument.
   *
   * @param t the function argument
   * @return the function result
   */
  @Override
  default T apply(T t) {
    return t;
  }

  /**
   * Hack to give access to producer for child modifier
   *
   * @param producer Producer that handles string
   */
  default void setProducer(Producer<String, String> producer) {
  }

  /**
   * Hack to give access to producer for child modifier
   *
   * @param consumer Kafka Consumer of String keys and String values
   */
  default void setConsumer(Consumer<String, String> consumer) {
  }
}
