package gms.shared.frameworks.test.utils.containers;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse.ContainerState;
import com.github.dockerjava.api.exception.BadRequestException;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Network;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.core.command.PullImageResultCallback;
import gms.shared.frameworks.test.utils.services.GmsServiceType;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class GmsDeploymentContainer<SELF extends GmsDeploymentContainer<SELF>> extends
  DockerComposeContainer<SELF> implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(GmsDeploymentContainer.class);

  private static final long KAFKA_TIMEOUT = 5;
  private static final String KAFKA_BOOTSTRAP_SERVERS = "kafka1:9092";
  private static final String BOOT_STRAP_SERVER_ARGUMENT = "--bootstrap-server";
  private static final String KAFKA_TOPIC_ARGUMENT = "--topic";
  private static final String FROM_BEGINNING_ARGUMENT = "--from-beginning";
  private static final String MAX_MESSAGES_ARGUMENT = "--max-messages";
  private static final String TIMEOUT_ARGUMENT = "--timeout-ms";

  private final EnumMap<GmsServiceType, Integer> ports;
  private final List<GmsServiceType> services;
  private final DockerClient docker;
  private boolean started;
  private final String deploymentProjectName;
  private List<String> images;

  public GmsDeploymentContainer(String identifier, File composeFile) {
    super(identifier, composeFile);
    deploymentProjectName = identifier;
    ports = new EnumMap<>(GmsServiceType.class);
    ports.put(GmsServiceType.DATAMAN, 8100);
    ports.put(GmsServiceType.POSTGRES_SERVICE, 5432);
    ports.put(GmsServiceType.OSD_SERVICE, 8080);
    ports.put(GmsServiceType.KAFKA_ONE, 9092);
    ports.put(GmsServiceType.PROCESSING_CONFIG_SERVICE, 8080);
    ports.put(GmsServiceType.SOH_CONTROL, 8080);
    ports.put(GmsServiceType.ZOOKEEPER, 2181);
    ports.put(GmsServiceType.CONNMAN, 8041);
    ports.put(GmsServiceType.RSDF_STREAMS_PROCESSOR, 8080);
    images = new ArrayList<>();
    try (var lineStream = Files.lines(Paths.get(composeFile.getPath()))) {
      var lines = lineStream
        .map(String::strip)
        .map(line -> line.replace("\"", ""))
        .filter(line -> line.contains("image:"))
        .collect(Collectors.toList());
      lines.stream()
        .forEach(line ->
          images.add(line.substring("image: ".length())));
    } catch (IOException e) {
      e.printStackTrace();
    }
    started = false;
    services = new ArrayList<>();
    docker = DockerClientFactory.instance().client();
  }

  /**
   * Add the subset of services, you would want to start from the provided compose environment.
   *
   * @param types variable number of {@link GmsServiceType} enumerations representing the different
   * services we can start
   * @return the current instance of {@link GmsDeploymentContainer} we are building.
   */
  public SELF withServices(GmsServiceType... types) {
    services.addAll(Arrays.stream(types).collect(Collectors.toList()));
    return (SELF) this;
  }

  /**
   * starts the compose environment
   */
  @Override
  public void start() {
    // always pull the latest images
    final var ciDockerRegistryEnvVarName = "CI_DOCKER_REGISTRY";
    final var dockerImageTagEnvVarName = "DOCKER_IMAGE_TAG";
    final var dockerRegistry = System.getenv(ciDockerRegistryEnvVarName);
    if (!"local".equals(dockerRegistry)) {
      pullDockerImages();
    }

    this.withEnv(ciDockerRegistryEnvVarName, dockerRegistry)
      .withEnv(dockerImageTagEnvVarName, System.getenv(dockerImageTagEnvVarName))
      .withPull(true)
      .withLocalCompose(true);

    if (services.isEmpty()) {
      services.addAll(Arrays.asList(GmsServiceType.values()));
    }
    if (!started) {
      Arrays.stream(GmsServiceType.values())
        .filter(type -> !services.contains(type))
        .forEach(type -> this.withScaledService(type.toString(), 0));
      services
        .forEach(type -> this.withScaledService(type.toString(), 1));
      services.stream()
        .filter(type -> !Set.of(GmsServiceType.ETCD,
          GmsServiceType.ACEI_MERGE_PROCESSOR,
          GmsServiceType.POSTGRES_EXPORTER,
          GmsServiceType.CONFIG_LOADER,
          GmsServiceType.DATAPROVIDERFILE,
          GmsServiceType.DATAPROVIDERKAFKA,
          GmsServiceType.OSD_RSDF_KAFKA_CONSUMER,
          GmsServiceType.OSD_STATION_SOH_KAFKA_CONSUMER,
          GmsServiceType.BASTION,
          GmsServiceType.SOH_CONTROL,
          GmsServiceType.UI_PROCESSING_CONFIGURATION_SERVICE,
          GmsServiceType.INTERACTIVE_ANALYSIS_API_GATEWAY,
          GmsServiceType.INTERACTIVE_ANALYSIS_UI
        ).contains(type))
        .forEach(type ->
          this
            .withExposedService(type.toString(), ports.get(type))
            .waitingFor(type.toString(),
              Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(120L))));

      services.stream()
        .filter(type ->
          Set.of(GmsServiceType.KAFKA_ONE)
            .contains(type))
        .findAny()
        .ifPresent(type ->
          this.waitingFor(type.toString(),
            new LogMessageWaitStrategy().withRegEx("^Created\\s+topic.*").withTimes(1)
              .withStartupTimeout(Duration.ofSeconds(120L))));

      services.stream()
        .filter(type -> type == GmsServiceType.CONFIG_LOADER)
        .findFirst()
        .ifPresent(type ->
          this.waitingFor(type.toString(),
            new LogMessageWaitStrategy()
              .withRegEx(".*Dataload completed successfully.*")
              .withStartupTimeout(Duration.ofSeconds(120L))));

      this.withServices(services.stream()
        .map(GmsServiceType::toString)
        .collect(Collectors.toList())
        .toArray(String[]::new));
      super.start();

    }

    started = true;
  }

  /**
   * Closes the container
   */
  @Override
  public void close() {
    if (started) {
      super.close();
    }
    started = false;
  }

  /**
   * Grab the hostname for the given service
   *
   * @param type {@link GmsServiceType} whose hostname we want to retrieve from the compose
   * environment.
   * @return {@link String} object representing the host name (or IP address in the environment for
   * the given host)
   */
  public String getServiceHost(GmsServiceType type) {
    return super.getServiceHost(type.toString(), ports.get(type));
  }

  /**
   * Grab the port assigned to the given service's public port
   *
   * @param type {@link GmsServiceType} whose hostname we want to retrieve from the compose}
   * @return Integer object representing the port number for the given service mapped to the public
   * port for the given service in the compose environment that is exposed externally.
   */
  public Integer getServicePort(GmsServiceType type) {
    return super.getServicePort(type.toString(), ports.get(type));
  }

  /**
   * Get the deployment's network
   *
   * @return the current deployment's {@link Optional} of type {@link Network}
   */
  public Optional<Network> getDeploymentNetwork() {
    return docker
      .listNetworksCmd()
      .exec()
      .stream()
      .filter(network -> network.getName()
        .contains(deploymentProjectName))
      .findAny();
  }

  /**
   * return the JDBC URL for the database
   *
   * @return {@link String} representing the connection string one needs to use to connect to the
   * database that is defined in the compose environment.
   */
  public String getJdbcUrl() {
    return String
      .format("jdbc:postgresql://%s:%d/gms", getServiceHost(GmsServiceType.POSTGRES_SERVICE),
        getServicePort(GmsServiceType.POSTGRES_SERVICE));
  }

  /**
   * return the set of kafka topics available in the kafka cluster.
   *
   * @return set representing all kafka topics available in the kafka cluster.
   */
  public Set<String> getKafkaTopics() {
    Optional<ExecCreateCmdResponse> response = sendServiceCommand(GmsServiceType.KAFKA_ONE,
      List.of("kafka-topics.sh", "--list", BOOT_STRAP_SERVER_ARGUMENT, KAFKA_BOOTSTRAP_SERVERS));
    if (response.isPresent()) {
      try {
        var stdout = new ByteArrayOutputStream();
        var stderr = new ByteArrayOutputStream();
        docker.execStartCmd(response.get().getId()).exec(
            new ExecStartResultCallback(stdout, stderr)).
          awaitCompletion(KAFKA_TIMEOUT, TimeUnit.SECONDS);
        return Set.copyOf(
          Arrays
            .asList(new String(stdout.toByteArray(), StandardCharsets.UTF_8).split("\n")));
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
    return Set.of();
  }

  /**
   * checking to see if the given service has been created by docker or not.
   *
   * @param service : {@link GmsServiceType} of the given service we're querying for
   * @return true if the service has been created, false otherwise.
   */
  public boolean isServiceCreated(GmsServiceType service) {
    return docker.listContainersCmd().exec().stream()
      .anyMatch(container -> Arrays.stream(container.getNames())
        .anyMatch(name -> name.contains(service.toString())));
  }

  /**
   * checking to see if the given service container is running in docker or not.
   *
   * @param service : {@link GmsServiceType} of the given service we're querying for
   * @return true if the service is running, false otherwise.
   */
  public boolean isServiceRunning(GmsServiceType service) {
    var container = getServiceContainer(service);
    if (container.isPresent()) {
      var response = docker.inspectContainerCmd(container.get().getId()).exec();
      return Boolean.TRUE.equals(response.getState().getRunning());
    } else {
      return false;
    }
  }

  /**
   * Determine whether or not a service is healthy. For it to be healthy, a container must be
   * running and its health status must be &quot;healthy&quot;
   */
  public boolean isServiceHealthy(GmsServiceType service) {
    Optional<Container> containerOpt = getServiceContainer(service);
    if (containerOpt.isPresent()) {
      InspectContainerResponse response = docker.inspectContainerCmd(
          containerOpt.get().getId())
        .exec();
      ContainerState state = response.getState();
      if (Boolean.TRUE.equals(state.getRunning())) {
        String healthStatus = state.getHealth().getStatus();
        return "healthy".equals(healthStatus);
      }
    }
    return false;
  }

  private void pullDockerImages() {
    final var ciDockerRegistry = System.getenv("CI_DOCKER_REGISTRY");
    final var dockerImageTag = System.getenv("DOCKER_IMAGE_TAG");
    logger.info("Pulling images");
    for (String imageName : images) {
      String image = imageName
        .replace("${CI_DOCKER_REGISTRY}", ciDockerRegistry)
        .replace("${DOCKER_IMAGE_TAG}", dockerImageTag);
      String tag = null;
      String registry = image;
      if (registry.contains(":")) {
        var info = registry.split(":");
        registry = info[0];
        tag = info[1];
      }
      var req = docker.pullImageCmd(registry);
      if (tag != null) {
        req.withTag(tag);
      }
      try {
        req.exec(new PullImageResultCallback()).awaitCompletion();
      } catch (InterruptedException | BadRequestException e) {
        logger.warn(String.format("Latest image %s not pull from registry", imageName), e);
        Thread.currentThread().interrupt();
      }
    }
  }

  /**
   * Sends kafka messages to a topic. Each nonblank line in the specified source is sent as a
   * separate message.
   *
   * @param filePath path to the message source.
   */
  public boolean sendKafkaMessages(String topicName, String filePath) {
    try {
      return sendKafkaMessages(topicName, Files.lines(Paths.get(filePath)));
    } catch (IOException e) {
      logger.error(e.getMessage());
    }
    return false;
  }

  /**
   * Sends a collection of messages to a kafka topic.
   */
  public boolean sendKafkaMessages(String topicName, Collection<String> messages) {
    return sendKafkaMessages(topicName, messages.stream());
  }

  /**
   * Sends a stream of messages to a kafka topic.
   */
  public boolean sendKafkaMessages(String topicName, Stream<String> messages) {
    var cmdResponse = sendServiceCommand(GmsServiceType.KAFKA_ONE,
      List.of("kafka-console-producer.sh", "--broker-list", KAFKA_BOOTSTRAP_SERVERS,
        KAFKA_TOPIC_ARGUMENT,
        topicName));
    if (cmdResponse.isPresent()) {

      final byte[] bytes = messages
        .map(String::trim)                          // Trim each line
        .filter(s -> !s.isEmpty())                  // Filter out blank lines.
        .collect(Collectors.joining("\n")) // Join with returns, so each will be
        .getBytes(StandardCharsets.UTF_8);          //    published as a separate message.

      try (var stdIn = new ByteArrayInputStream(bytes)) {
        var stdOut = new ByteArrayOutputStream();
        var stdErr = new ByteArrayOutputStream();
        docker.execStartCmd(cmdResponse.get().getId())
          .withStdIn(stdIn)
          .exec(new ExecStartResultCallback(stdOut, stdErr))
          .awaitCompletion(KAFKA_TIMEOUT, TimeUnit.SECONDS);
        return true;
      } catch (IOException e) {
        logger.error(e.getMessage());
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      }

    }

    return false;
  }

  /**
   * Receives a list of messages from a kafka topic with an optional timeout.
   */
  public List<String> receiveKafkaMessages(String topicName, int maxMessages, int timeoutMs) {
    var cmdResponse = sendServiceCommand(GmsServiceType.KAFKA_ONE,
      List.of("kafka-console-consumer.sh", BOOT_STRAP_SERVER_ARGUMENT, KAFKA_BOOTSTRAP_SERVERS,
        KAFKA_TOPIC_ARGUMENT,
        topicName, FROM_BEGINNING_ARGUMENT, MAX_MESSAGES_ARGUMENT,
        String.valueOf(maxMessages), TIMEOUT_ARGUMENT, Integer.toString(timeoutMs)));
    if (cmdResponse.isPresent()) {
      var stdout = new ByteArrayOutputStream();
      var stderr = new ByteArrayOutputStream();

      try {
        docker.execStartCmd(cmdResponse.get().getId())
          .exec(new ExecStartResultCallback(stdout, stderr)).awaitCompletion(
            KAFKA_TIMEOUT, TimeUnit.SECONDS);

        // Return as a list, so they will be in the order received.
        return Arrays.asList(new String(stdout.toByteArray()).split("\n"));
      } catch (InterruptedException e) {
        logger.error(e.getMessage());
        Thread.currentThread().interrupt();
      }
    }
    return Collections.emptyList();
  }

  /**
   * Retrieve the committed offset of messages that have been consumed
   *
   * @param topicName topic to get offset for
   * @return
   */
  public List<String> getTopicOffset(String topicName) {

    var cmdResponse = sendServiceCommand(GmsServiceType.KAFKA_ONE,
      List.of("kafka-run-class.sh", "kafka.tools.GetOffsetShell",
        "--broker-list", KAFKA_BOOTSTRAP_SERVERS,
        KAFKA_TOPIC_ARGUMENT, topicName));
    if (cmdResponse.isPresent()) {
      var stdout = new ByteArrayOutputStream();
      var stderr = new ByteArrayOutputStream();

      try {
        docker.execStartCmd(cmdResponse.get().getId())
          .exec(new ExecStartResultCallback(stdout, stderr)).awaitCompletion(
            KAFKA_TIMEOUT, TimeUnit.SECONDS
          );
        // Return as a list, so they will be in the order received.
        return Arrays.asList(new String(stdout.toByteArray()).split("\n"));
      } catch (InterruptedException e) {
        logger.warn(String.format("Issue retrieving Kafka Topic Offset for topic %s", topicName), e);
        Thread.currentThread().interrupt();
      }
    }
    return Collections.emptyList();

  }

  /**
   * Get the list consumer groups
   *
   * @return a {@link List} of consumer groups
   */
  public List<String> getKafkaConsumerGroups() {
    var cmdResponse = sendServiceCommand(GmsServiceType.KAFKA_ONE,
      List.of("kafka-consumer-groups.sh", BOOT_STRAP_SERVER_ARGUMENT, KAFKA_BOOTSTRAP_SERVERS,
        "--list"));
    if (cmdResponse.isPresent()) {
      var stdout = new ByteArrayOutputStream();
      var stderr = new ByteArrayOutputStream();

      try {
        docker.execStartCmd(cmdResponse.get().getId())
          .exec(new ExecStartResultCallback(stdout, stderr)).awaitCompletion(
            KAFKA_TIMEOUT, TimeUnit.SECONDS
          );
        // Return as a list, so they will be in the order received.
        return Arrays.asList(new String(stdout.toByteArray()).split("\n"));
      } catch (InterruptedException e) {
        logger.warn("Error retrieving consumer groups", e);
        Thread.currentThread().interrupt();
      }
    }
    return Collections.emptyList();
  }

  /**
   * Get the lag on each topic partition based on consumer group
   *
   * @param groupName the group name for which to retrieve a consumer group lag
   * @return a {@link Map} of topic to a {@link Map} of partition to lag
   */
  public Map<String, Map<Integer, Integer>> getConsumerGroupLag(String groupName) {

    var cmdResponse = sendServiceCommand(GmsServiceType.KAFKA_ONE,
      List.of("kafka-consumer-groups.sh", BOOT_STRAP_SERVER_ARGUMENT, KAFKA_BOOTSTRAP_SERVERS,
        "--group",
        groupName, "--describe"));
    if (cmdResponse.isPresent()) {
      return calculateConsumerGroupLag(cmdResponse.get());
    }
    return Collections.emptyMap();
  }

  private HashMap<String, Map<Integer, Integer>> calculateConsumerGroupLag(ExecCreateCmdResponse cmdResponse) {
    var topicPartitionLagMapping = new HashMap<String, Map<Integer, Integer>>();
    var stdout = new ByteArrayOutputStream();
    var stderr = new ByteArrayOutputStream();
    try {
      docker.execStartCmd(cmdResponse.getId())
        .exec(new ExecStartResultCallback(stdout, stderr)).awaitCompletion(
          KAFKA_TIMEOUT, TimeUnit.SECONDS
        );
      var outputArray = new String(stdout.toByteArray()).split("\n");
      IntStream.range(2, outputArray.length).mapToObj(i -> outputArray[i].split("\\s+"))
        .forEach(lineArray -> {
          var lineArrayLength = lineArray.length;
          if (lineArrayLength > 4) {
            if (topicPartitionLagMapping.containsKey(lineArray[0])) {
              topicPartitionLagMapping.get(lineArray[0]).put(Integer.valueOf(lineArray[1]),
                Integer.valueOf(lineArray[(lineArrayLength - 4)]));
            } else {
              var partitionLagMapping = new HashMap<Integer, Integer>();
              if (lineArray[(lineArrayLength - 4)].startsWith("-")) {
                partitionLagMapping.put(Integer.valueOf(lineArray[1]), 0);
              } else {
                partitionLagMapping.put(Integer.valueOf(lineArray[1]),
                  Integer.valueOf(lineArray[(lineArrayLength - 4)]));
              }
              topicPartitionLagMapping.put(lineArray[0], partitionLagMapping);
            }
          }
        });
      return topicPartitionLagMapping;
    } catch (InterruptedException e) {
      logger.error("Unable to obtain consumer group details", e);
      Thread.currentThread().interrupt();
    }
    return topicPartitionLagMapping;
  }

  /**
   * restarts a given service in the compose environment
   */
  public boolean restartService(GmsServiceType serviceType) {
    var service = getServiceContainer(serviceType);
    if (service.isPresent() && isServiceRunning(serviceType)) {
      docker.restartContainerCmd(service.get().getId()).withtTimeout(5).exec();
      return isServiceHealthy(serviceType);
    }
    return true;
  }

  public boolean restartServicewithRetryBackoff(GmsServiceType serviceType, int maxDelay) {

    var service = getServiceContainer(serviceType);
    if (service.isPresent() && isServiceRunning(serviceType)) {
      docker.restartContainerCmd(service.get().getId()).withtTimeout(5).exec();
    }
    RetryPolicy<Optional<String>> retryPolicy
      = new RetryPolicy<Optional<String>>()
      .withBackoff(50, maxDelay, ChronoUnit.MILLIS)
      .withMaxAttempts(100)
      .handle(Exception.class)
      .onFailedAttempt(e -> logger.warn("Failed to get healthy service, will try again..."));

    Failsafe.with(retryPolicy)
      .onFailure(e -> logger.error("Unable to restart service in healthy state"))
      .run(() -> throwIfNotHealthy(serviceType));

    logger.info("Service is healthy and restarted");
    return isServiceHealthy(serviceType);

  }

  private void throwIfNotHealthy(GmsServiceType serviceType) {
    if (!isServiceHealthy(serviceType)) {
      throw new IllegalStateException();
    }
  }

  private Optional<ExecCreateCmdResponse> sendServiceCommand(GmsServiceType service,
    List<String> cmd) {
    Optional<Container> serviceContainer = getServiceContainer(service);
    if (serviceContainer.isPresent()) {
      return Optional.of(docker.execCreateCmd(serviceContainer.get().getId())
        .withAttachStdin(true)
        .withAttachStderr(true)
        .withAttachStdout(true)
        .withCmd(cmd.toArray(String[]::new))
        .exec());
    }
    return Optional.empty();
  }

  private Optional<Container> getServiceContainer(GmsServiceType service) {
    return docker.listContainersCmd().exec().stream()
      .filter(container -> Arrays.stream(container.getNames())
        .anyMatch(
          name -> name.contains(deploymentProjectName) && name.contains(service.toString())))
      .findAny();
  }

  /**
   * Get the URL for the specified service.
   *
   * @param serviceType {@link GmsServiceType} of the service.
   * @return a string version of the URL.
   * @throws NoSuchElementException if the service is undefined.
   */
  // Package private to facilitate unit testing, since environment vars cannot be
  // modified via code.
  public String getServiceURL(GmsServiceType serviceType) {
    return String.format("http://%s:%d", getServiceHost(serviceType), getServicePort(serviceType));
  }

  public DockerClient getDocker() {
    return docker;
  }

}

