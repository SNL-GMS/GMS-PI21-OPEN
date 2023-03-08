package gms.core.performancemonitoring.ssam.control.dataprovider;

public interface ReactiveConsumerBuilder {

  <T> ReactiveConsumer<T> build(Class<T> clazz);

  ReactiveConsumerBuilder withTopic(String topic);

}
