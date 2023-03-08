package gms.shared.frameworks.test.utils.config;

import gms.shared.frameworks.systemconfig.SystemConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;

/**
 * Class providing retrieval of system configuration values from a properties.
 *
 * <p>If the properties object provided is not present or empty, this interface will be still be
 * valid but will return no configuration values. A warning will be logged for this case.
 */
public class PropertyConfigRepository implements SystemConfigRepository {

  private static final Logger logger = LoggerFactory.getLogger(PropertyConfigRepository.class);
  private final Properties properties;

  /**
   * Instantiate a PropertyConfigRepository
   */
  private PropertyConfigRepository(Properties properties) {
    if (properties == null || properties.isEmpty()) {
      logger.warn("No properties specified for system configuration.");
      this.properties = new Properties();
    } else {
      this.properties = properties;
    }
  }

  /**
   * @param key key name to return the value for from this repository
   * @return value of key if present, empty Optional if not found
   */
  @Override
  public Optional<String> get(String key) {
    return Optional.ofNullable(properties.getProperty(key));
  }

  /**
   * Construct a builder for a PropertyConfigRepository.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builder for a PropertyConfigRepository.
   */
  public static class Builder {

    private Properties properties = new Properties();

    /**
     * Set the properties for the PropertyConfigRepository under construction.
     *
     * @param properties from which to read configuration.
     */
    public Builder setProperties(Properties properties) {
      this.properties = properties;
      return this;
    }

    /**
     * Finish construction of a new PropertyConfigRepository
     *
     * @return newly constructed PropertyConfigRepository
     */
    public PropertyConfigRepository build() {
      return new PropertyConfigRepository(properties);
    }
  }
}
