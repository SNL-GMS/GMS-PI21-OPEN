package gms.shared.frameworks.injector;

import org.kohsuke.args4j.Option;

import java.io.File;

public class DataInjectorArguments {

  @Option(name = "--interval", required = true, usage = "Interval on which to inject data, as a duration")
  private String interval;

  @Option(name = "--initialDelay", usage = "The initial delay before emitting the first value. Defaults to 0")
  private long initialDelay = 0;

  @Option(name = "--type", required = true, usage = "The class name of the Java type that will be injected")
  private InjectableType type;

  @Option(name = "--base", required = true, usage = "The json file containing the basic type to inject")
  private File base;

  @Option(name = "--topic", required = true, usage = "Kafka topic to push data to")
  private String topic;

  @Option(name = "--batchCount", usage = "The total number of batches to emit. Runs indefinitely if not set.")
  private Integer batchCount = null;

  @Option(name = "--batchSize", usage = "Number of items to emit per interval.  Defaults to 1")
  private int batchSize = 1;

  @Option(name = "--bootstrapServer", required = true, usage = "Kafka bootstrap server to connect to")
  private String bootstrapServer;

  @Option(name = "--retries", usage = "Number of times to retry connecting to Kafka")
  private int retries = 5;

  @Option(name = "--retryBackoff", usage = "Number of milliseconds. Defaults to 3000")
  private long retryBackoff = 3000;

  public String getInterval() {
    return interval;
  }

  public long getInitialDelay() {
    return initialDelay;
  }

  public InjectableType getType() {
    return type;
  }

  public File getBase() {
    return base;
  }

  public String getTopic() {
    return topic;
  }

  public Integer getBatchCount() {
    return batchCount;
  }

  public int getBatchSize() {
    return batchSize;
  }

  public String getBootstrapServer() {
    return bootstrapServer;
  }

  public int getRetries() {
    return retries;
  }

  public long getRetryBackoff() {
    return retryBackoff;
  }

}
