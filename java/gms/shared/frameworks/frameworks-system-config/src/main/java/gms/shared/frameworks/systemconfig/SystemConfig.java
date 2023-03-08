package gms.shared.frameworks.systemconfig;

import gms.shared.frameworks.common.config.ServerConfig;
import gms.shared.utilities.validation.PathValidation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Optional;

/**
 * Provides access to general GMS system configuration. System configuration may include values such
 * as log levels, database connection parameters, service routes, thread pool sizes, etc. but does
 * not include any algorithm specific configuration values.
 *
 * <p>Retrieves configuration values from a {@link SystemConfigRepository}.
 *
 * <p>Key names have an inferred name space using a dot ('.') as a separator. The first part of the
 * name (before the dot) is the name of the component. The more specific value with the component
 * name will override the more general value if present.
 *
 * <p>Consider the following example:
 *
 * <p>We may define a value for the key 'port' as follows:
 *
 * <pre>{@code
 * port = 8080
 * }</pre>
 * <p>
 * While most components want to share this value, we may want to define a <i>different</i>
 * value specific to the 'spacemodulator' component (perhaps as a temporary override for
 * development or testing). We can do that by prefixing the component name:
 *
 * <pre>{@code
 * spacemodulator.port = 591
 * }</pre>
 * <p>
 * Thus the 'spacemodulator' component can always just call {@code getValueAsInt('port')}. This
 * will first look for the value of port with our component name ('spacemodulator.port') and, failing
 * to find that, will then just return the value of 'port'.
 *
 * <p>Alternatively, the caller specify a key name with a desired prefix already included.
 *
 * <p>For instance, the 'timeinhibitor' component may need to look up the port for the
 * 'spacemodulator'. In this case, it would call {@code getValueAsInt("spacemodulator.port")}. This
 * would resolve as if we had done a lookup of 'port' with the prefix of 'spacemodulator'
 */
public class SystemConfig {

  /**
   * The name of the configuration item indicating the client timeout.
   */
  public static final String CLIENT_TIMEOUT = "client-timeout";

  /**
   * The name of the configuration item indicating a service host identifier.
   */
  public static final String HOST = "host";

  /**
   * The name of the configuration item indicating the port at which a service is provided.
   */
  public static final String PORT = "port";

  /**
   * The name of the configuraton item indicating the duration before a request should time out.
   */
  public static final String IDLE_TIMEOUT = "idle-timeout";

  /**
   * The name of the configuration item indicating the minimum number of threads a service process
   * should instantiate to handling requests.
   */
  public static final String MIN_THREADS = "min-threads";

  /**
   * The name of the configuration item indicating the maximum number of threads a service process
   * should instantiate for handling requests.
   */
  public static final String MAX_THREADS = "max-threads";

  /**
   * The name of the configuration item indicating the location of the processing configuration for
   * a component.
   */
  public static final String PROCESSING_CONFIGURATION_ROOT = "processing-configuration-root";

  // We must use apache log4j to have custom log levels
  private static final Logger logger = LogManager.getLogger(SystemConfig.class);
  private final String componentName;
  private final List<SystemConfigRepository> repositories;

  /**
   * Creates a new {@link SystemConfig} for a component with the provided name.
   *
   * @param componentName component name, not null
   * @throws NullPointerException if componentName is null
   */
  public static SystemConfig create(String componentName) {
    return new SystemConfig(componentName, SystemConfigRepositoryDefaultFactory.create());
  }

  /**
   * Creates a new {@link SystemConfig} for a component with the provided name. Values are
   * gathered from the provided repository.
   *
   * @param componentName component name, not null
   * @param repository the repository to retrieve values from
   * @throws NullPointerException if componentName is null
   */
  public static SystemConfig create(String componentName, SystemConfigRepository repository) {
    return new SystemConfig(componentName, Collections.singletonList(repository));
  }

  /**
   * Creates a new {@link SystemConfig} for a component with the provided name. Values are
   * gathered from the provided list of repositories. Values found in repositories toward the front
   * of the list will override values in repositories toward the end of the list.
   *
   * @param componentName component name, not null
   * @param repositories the repository to retrieve values from
   * @throws NullPointerException if componentName is null
   */
  public static SystemConfig create(String componentName, List<SystemConfigRepository> repositories) {
    return new SystemConfig(componentName, repositories);
  }

  private SystemConfig(String componentName, List<SystemConfigRepository> repositories) {
    Validate.notBlank(componentName, "SystemConfig cannot be created with a null or empty componentName");
    Objects.requireNonNull(repositories, "SystemConfig cannot be created with a null repository");

    logger.info(
      "system-configuration: creating system configuration");

    this.componentName = componentName;
    this.repositories = Collections.unmodifiableList(repositories);
    try {
      var logLevel = Level.getLevel(getValue("log_level"));
      //This occurs if there is a value defined in SystemConfig that has not been defined in the log4j confg file
      //in this case we just use whatever is defined log4j config file
      if (logLevel == null) {
        logger.warn("The specified LogLevel has not been defined in the log4j.xml configuration file.  " +
          "Defaulting to the level defined in log4j.xml file");
      } else {
        Configurator.setAllLevels(LogManager.getRootLogger().getName(), logLevel);
      }
      //this is thrown if the value is not defined in SystemConfig...
      //in this case we just use what's in Log4j
    } catch (MissingResourceException e) {
      logger.info("No log_level defined in SystemConfig.  Defaulting to the level defined in log4j.xml file.");
    }
  }

  /**
   * Get the component name for this {@link SystemConfig}.
   *
   * @return component name for this {@link SystemConfig}
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Get the value of the specified key (if present) from the GMS system configuration.
   *
   * @param label key to a single configuration value, not null
   * @return value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key n
   */
  public String getValue(String label) {
    Objects.requireNonNull(label, "System configuration key can not be null");
    // Search through each repository for the requested key and return the first found value.
    for (SystemConfigRepository r : repositories) {
      Optional<String> value = r.search(label, getComponentName());
      if (value.isPresent()) {
        return value.get().trim();
      }
    }
    throw new MissingResourceException("Configuration not found", getComponentName(), label);
  }

  /**
   * Get the integer value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return integer value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws NumberFormatException if value can not be parsed as an integer
   */
  public int getValueAsInt(String key) {
    return Integer.parseInt(getValue(key));
  }

  /**
   * Get the integer value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return long value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws NumberFormatException if value can not be parsed as an long
   */
  public long getValueAsLong(String key) {
    return Long.parseLong(getValue(key));
  }

  /**
   * Get the double value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return double value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws NumberFormatException if value can not be parsed as an double
   */
  public double getValueAsDouble(String key) {
    return Double.parseDouble(getValue(key));
  }

  /**
   * Get the boolean value of the specified key (if present) from the GMS system configuration.
   *
   * @param key key to a single configuration value, not null
   * @return boolean value for specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws IllegalArgumentException if value can not be parsed as an boolean
   */
  public boolean getValueAsBoolean(String key) {
    // Note: not using Boolean.parseBoolean so we can support 1/0 and yes/no values in addition to
    // true/false.
    String value = getValue(key);
    switch (value.toLowerCase(Locale.ENGLISH)) {
      case "true":
      case "yes":
      case "1":
        return true;
      case "false":
      case "no":
      case "0":
        return false;
      default:
        throw new IllegalArgumentException(
          value + "does not represent a boolean value of either \"true\" or \"false\"");
    }
  }

  /**
   * Get the {@link Path} represented by the specified key (if present) from the GMS system
   * configuration.
   *
   * @param key key to a single configuration value, not null
   * @return Path object represented by the specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws IllegalArgumentException if the returned path does not have the expected base path
   */
  public Path getValueAsPath(String key) {

    var pathString = getValue(key);
    return PathValidation.getValidatedPath(pathString, "/rsdf");
  }

  /**
   * Get the {@link Duration} represented by the specified key (if present) from the GMS system
   * configuration.
   *
   * @param key key to a single configuration value, not null
   * @return {@link Duration} object represented by the specified key if present
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if value is not found for the given key
   * @throws IllegalArgumentException if value can not be parsed as a {@link Duration}
   */
  public Duration getValueAsDuration(String key) {
    return Duration.parse(getValue(key));
  }

  /**
   * Get the {@link URL} for this component from the GMS system configuration.
   *
   * @return URL for the component this configuration is for
   * @throws MissingResourceException if host or port cannot be found
   * @throws IllegalArgumentException if retrieved host or port from config are invalid
   */
  public URL getUrl() {
    return makeUrl(getValue(SystemConfig.HOST), getValueAsInt(SystemConfig.PORT));
  }

  /**
   * Get the {@link URL} for the specified component from the GMS system configuration.
   *
   * @param componentName the name of the component
   * @return URL for the component
   * @throws NullPointerException if key is null
   * @throws MissingResourceException if host or port cannot be found for the component
   * @throws IllegalArgumentException if retrieved host or port from config are invalid
   */
  public URL getUrlOfComponent(String componentName) {
    return makeUrl(getValue(createKey(componentName, SystemConfig.HOST)),
      getValueAsInt(createKey(componentName, SystemConfig.PORT)));
  }

  private static URL makeUrl(String host, int port) {
    final var urlStr = String.format("http://%s:%d", host, port);
    try {
      return new URL(urlStr);
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Malformed URL: " + urlStr, e);
    }
  }

  /**
   * Gets a {@link ServerConfig} object for this configuration.
   *
   * @return a {@link ServerConfig}, not null
   */
  public ServerConfig getServerConfig() {
    return ServerConfig.from(
      getValueAsInt(SystemConfig.PORT),
      getValueAsInt(SystemConfig.MIN_THREADS),
      getValueAsInt(SystemConfig.MAX_THREADS),
      getValueAsDuration(SystemConfig.IDLE_TIMEOUT));
  }


  static String createKey(String prefix, String key) {
    return prefix + SystemConfigConstants.SEPARATOR + key;
  }
}
