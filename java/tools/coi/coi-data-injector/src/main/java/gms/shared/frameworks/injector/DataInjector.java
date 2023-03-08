package gms.shared.frameworks.injector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class DataInjector {

  private static final Logger LOGGER = LoggerFactory.getLogger(DataInjector.class);

  public static void main(String[] args) throws CmdLineException,
    NoSuchMethodException, IllegalAccessException, InvocationTargetException,
    InstantiationException, InterruptedException {
    var arguments = new DataInjectorArguments();
    new CmdLineParser(arguments).parseArgument(args);

    var mapper = CoiObjectMapperFactory.getJsonObjectMapper();
    Producer<String, String> producer = KafkaProducerFactory.getProducer(
      "mock_data_inject_producer_" + arguments.getType().name(),
      arguments.getBootstrapServer(),
      arguments.getRetries(),
      arguments.getRetryBackoff());

    int totalCount;
    Optional<Integer> batchCount = Optional.ofNullable(arguments.getBatchCount());
    Consumer<String, String> consumer = KafkaConsumerFactory.getConsumer(
      "mock_data_inject_consumer_" + arguments.getType().name(),
      arguments.getBootstrapServer(),
      arguments.getRetries(),
      arguments.getRetryBackoff());

    var interval = Duration.parse(arguments.getInterval());
    Modifier modifier = arguments.getType().getModifier().getConstructor().newInstance();

    // TODO Temp hack to see capability rollup sent on topic
    modifier.setProducer(producer);
    modifier.setConsumer(consumer);

    Flux<Long> flux;

    if (batchCount.isPresent()) {
      totalCount = batchCount.get() * arguments.getBatchSize();
      // Flux.range emits a range of integers from 1 to totalCount
      // provided interval. We use the provided interval divided by the batch size so that we can
      // later collect them up, which takes numToEmit * interval/numToEmit time, thus producing the
      // desired interval. The sequence completes immediately after the last value
      // {@code (start + count - 1)} has been reached.
      flux = Flux.range(1, totalCount)
        // We then add an initial delay so nothing is emitted until after than time has
        // finished, on the
        .delaySequence(Duration.ofMillis(arguments.getInitialDelay()))
        // Then delay each element by the total interval / batch size (so that when they get
        // reassembled into a batch later
        .delayElements(interval.dividedBy(arguments.getBatchSize()))
        // Convert the integers int longs so that it can match up with the other flux
        .map(Integer::longValue);
    } else {
      // Flux.interval emits sequential, increasing longs on the provided interval.  We use the
      // provided interval divided by the batch size so that we can later collect them up, which
      // takes
      // numToEmit * interval/numToEmit time, thus producing the desired interval.
      flux = Flux.interval(Duration.ofMillis(interval.toMillis() / arguments.getBatchSize()));
    }

    // The supplier: convert the interval value into an object of the type being emitted,
    // using the json file to provide the initial object
    flux.map(val -> readValue(mapper, arguments.getBase(), arguments.getType().getBaseClass()))
      // Collect into a batch of numToEmit items (possibly 1) that will be put on the topic
      // Since this batches
      // by number of emits, it will force the data to be batched on the desired interval
      .buffer(arguments.getBatchSize())
      // The modifier: take the basic object and tweaks values so not all objects being
      // submitted to the topic are identical
      .map((Function<List<Object>, Iterable>) modifier::apply)
      // the buffer has effectively removed the delay between the individual items in the
      // list, so we can flatmap without changing the interval, so that we can then submit
      // them to Kafka individually
      .flatMap(Flux::fromIterable)
      // Move the work being done to the current thread, causing it to block and prevent
      // program termination.  Flux.interval is on the compute scheduler, so it won't
      // block and the program will finish before anything happens if we don't move the
      // subscription.
      // Put each item on the kafka topic, or log an error if one has occurred somewhere in
      // the flux.
      .doOnNext(next -> submitToKafka(arguments.getTopic(), next, producer))
      // Handle any errors.
      // Note that the likely errors are either from 1) the supplier being a
      // format other than json, or 2) the modifier expecting a data type other than that
      // produced by the supplier
      .doOnError(error -> LOGGER.error("Error processing data: {}", error))
      // Don't complete until the last item is reached, effectively blocking the main thread.
      .blockLast();
    //wait for onComplete to finish...last message wasn't being sent
    Thread.sleep(1000);
  }

  private static Object readValue(ObjectMapper mapper, File source, Class type) {
    try {
      return mapper.readValue(source, type);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  private static void submitToKafka(String topic, Object next, Producer<String, String> producer) {

    try {
      ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic,
        CoiObjectMapperFactory.getJsonObjectMapper().writeValueAsString(next));
      producer.send(producerRecord);
    } catch (JsonProcessingException ex) {
      throw new UncheckedIOException(ex);
    }
  }

}
