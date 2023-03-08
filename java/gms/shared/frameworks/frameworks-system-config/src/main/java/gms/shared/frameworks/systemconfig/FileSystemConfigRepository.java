package gms.shared.frameworks.systemconfig;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;

/**
 * Class providing retrieval of system configuration values from a single properties file.
 *
 * <p>If the file specified by filename is not present, this interface will still be valid but
 * will return no configuration values. A warning will be logged for this case.</p>
 */
public class FileSystemConfigRepository implements SystemConfigRepository {

  private static final Logger logger = LoggerFactory.getLogger(FileSystemConfigRepository.class);
  private final Properties properties = new Properties();
  private final String filePath;

  /**
   * Instantiate a FileSystemConfigRepository
   * Note: The filename should include all directory info from the base path on.
   * 
   */
  private FileSystemConfigRepository(String filename) {
    this.filePath = filename;
    if (StringUtils.isNotEmpty(filePath)) {
      try {
        try (var fis = new FileInputStream(filename);
             InputStream in = new BufferedInputStream(fis)) {
          this.properties.load(in);
        }
        logger.info("Read system configuration file.");
        logger.debug("System configuration file is '{}'", filePath);
      } catch (IOException e) {
        logger.info("Failed to read system configuration file {}: {}", filePath, e);
      }
    } else {
      logger.info("No filename specified for system configuration.");
    }
  }

  /**
   * File-specific implementation of get.
   *
   * @param key key name to return the value for from this repository
   * @return value of key if present, empty Optional if not found
   */
  @Override
  public Optional<String> get(String key) {
    return Optional.ofNullable(properties.getProperty(key));
  }

  /**
   * Get the name of this system configuration repository as a string.
   *
   * @return name of the form "file:filename"
   */
  @Override
  public String toString() {
    return "file:" + StringUtils.defaultString(filePath, "none");
  }

  /**
   * Construct a builder for a FileSystemConfigurationRepository.
   * @return a builder of the class
   */
  public static Builder builder() {
    return new FileSystemConfigRepository.Builder();
  }

  /**
   * Builder for a FileSystemConfigurationRepository.
   */
  public static class Builder {
    private static final String DEFAULT_FILENAME = "${HOME}/configuration-overrides.properties";
    private String filename = DEFAULT_FILENAME;

    /**
     * Set the filename for the FileSystemConfigurationRepository under construction.
     *
     * <p>If no filename is specified, this will default to
     * ${HOME}/configuration-overrides.properties"
     *
     * @param filename full path to properties file from which to read configuration.
     */
    public Builder setFilename(String filename) {
      this.filename = filename;
      return this;
    }

    /**
     * Finish construction of a new FileSystemConfigRepository
     *
     * @return newly constructed FileSystemConfigRepository
     */
    public FileSystemConfigRepository build() {
      return new FileSystemConfigRepository(filename);
    }
  }
}
