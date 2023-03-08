package gms.shared.frameworks.configuration.repository;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationOption;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.utilities.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Implements {@link ConfigurationRepository} by constructing {@link Configuration} from {@link
 * ConfigurationOption} files stored in files.
 * <p>
 * Assumes the following structure: 1. All data is rooted in a single configuration root directory.
 * This directory is provided to {@link FileConfigurationRepository#create(Path)}
 * <p>
 * 2. The configuration root directory has a collection of subdirectories but no files.  Each
 * subdirectory corresponds to a Configuration; the subdirectory name becomes the Configuration's
 * name.
 * <p>
 * 3. Each subdirectory contains .yaml files but no nested subdirectories.  Each .yaml file contains
 * a single serialized ConfigurationOption.
 * <p>
 * Only implements the {@link ConfigurationRepository#getKeyRange(String)} operation.
 */
public class FileConfigurationRepository implements ConfigurationRepository {

  private static final Logger logger = LoggerFactory.getLogger(FileConfigurationRepository.class);

  private final Map<String, Configuration> configurationByName;

  private FileConfigurationRepository(Map<String, Configuration> configurationByName) {
    this.configurationByName = configurationByName;
  }

  /**
   * Obtain a {@link FileConfigurationRepository} with the provided configurationRoot directory.
   * configurationRoot can be on an actual file system or packaged in a Jar (e.g. as occurs when the
   * files were originally in a src/resources directory).  If it is in a resources directory
   * configurationRoot must include the Jar's path.  If it is on an actual filesystem provide a path
   * to the configurationRoot directory.
   *
   * @param configurationRoot {@link Path} configuration root directory, not null
   * @return {@link FileConfigurationRepository}, not null
   * @throws NullPointerException if configurationRoot is null
   */
  public static FileConfigurationRepository create(Path configurationRoot) {
    Objects.requireNonNull(configurationRoot, "configurationRoot can't be null");

    var configurations = FileConfigurationRepository.loadConfigurations(configurationRoot).stream()
      .sorted(Comparator.comparing(Configuration::getName))
      .collect(Collectors.toList());

    final var cleansedString = Validation.cleanseInputString(configurationRoot.toString());
    logger.info("Configurations for {}: {}", cleansedString, configurations.size());
    configurations.forEach(configuration -> logger.info("Config {}: {}", configuration.getName(), configuration));

    return new FileConfigurationRepository(configurations.stream()
      .collect(Collectors.toMap(Configuration::getName,
        Function.identity(),
        (oldVal, newVal) -> oldVal)));
  }

  @Override
  public Optional<Configuration> get(String key) {
    return Optional.of(this.configurationByName.get(key));
  }

  @Override
  public Collection<Configuration> getKeyRange(String keyPrefix) {
    return this.configurationByName.entrySet().stream()
      .filter(e -> e.getKey().startsWith(keyPrefix))
      .map(Entry::getValue)
      .collect(Collectors.toList());
  }

  @Override
  public Optional<Configuration> put(Configuration configuration) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Configuration> putAll(Collection<Configuration> configurations) {
    throw new UnsupportedOperationException();
  }

  /**
   * Loads {@link Configuration}s from a base directory.  Constructs a Configuration for each
   * subdirectory in the baseDirectory.
   *
   * @param baseDirectory base directory containing the configurations
   * @return List of {@link Configuration} loaded from the baseDirectory
   */
  @SuppressWarnings("unchecked")
  private static List<Configuration> loadConfigurations(Path baseDirectory) {
    // Construct the correct type of DirectoryOperations for the provided path.  Wrap
    // the DirectoryOperations in a LoggingDirectoryOperations which logs which directories and
    // files get processed.
    boolean isResources = baseDirectory.toString().contains("file:") &&
      baseDirectory.toString().contains("!");

    DirectoryOperations directoryOperations = new LoggingDirectoryOperations(
      isResources ? new ResourcesDirectoryOperations() : new FilesystemDirectoryOperations());

    // JSON ObjectMapper
    var objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    List<Configuration> configurations = new ArrayList<>();

    // subDirectories become Configurations
    Collection<String> subDirectories = directoryOperations
      .getSubDirectories(baseDirectory);

    // Files in each subdirectory are the ConfigurationOptions
    for (String subDir : subDirectories) {
      FileConfigurationRepository.logger.info("Loading configuration from subdirectory {}", subDir);

      List<ConfigurationOption> configOptions = new ArrayList<>();
      for (String filename : directoryOperations.getFilesInDirectory(subDir)) {
        FileConfigurationRepository.logger.info("Loading configuration from file {}", filename);
        try {
          resolveConfigurationFromFieldMaps(directoryOperations, objectMapper, configOptions,
            filename);
        } catch (IllegalArgumentException | IOException e) {
          FileConfigurationRepository.logger.error("Could not load configuration from disk", e);
        }
      }

      String splitter = getSystemIndependentFileSeparator();

      String[] pathComponents = subDir.split(splitter);
      String configurationName = pathComponents[pathComponents.length - 1];
      configurations.add(Configuration.from(configurationName, configOptions));
    }

    FileConfigurationRepository.logger.debug("configurations: {}", configurations);
    return configurations;
  }

  private static String getSystemIndependentFileSeparator() {
    String splitter;
    if (File.separatorChar == '\\')
      splitter = "\\\\";
    else
      splitter = File.separator;
    return splitter;
  }

  private static void resolveConfigurationFromFieldMaps(DirectoryOperations directoryOperations,
    ObjectMapper objectMapper, List<ConfigurationOption> configOptions, String filename)
    throws IOException {
    try {
      var typeFactory = objectMapper.getTypeFactory();
      JavaType fieldMapType = typeFactory
        .constructMapType(HashMap.class, String.class, Object.class);
      JavaType fieldMapList = typeFactory.constructCollectionType(List.class, fieldMapType);

      List<Map<String, Object>> fieldMaps = objectMapper
        .readValue(directoryOperations.getUrl(filename),
          fieldMapList);
      fieldMaps.stream()
        .map(fieldMap -> FieldMapUtilities
          .fromFieldMap(fieldMap, ConfigurationOption.class))
        .forEach(configOptions::add);
    } catch (MismatchedInputException e) {
      configOptions.add(FieldMapUtilities.fromFieldMap(
        objectMapper.readValue(directoryOperations.getUrl(filename), Map.class),
        ConfigurationOption.class));
    }
  }

  /**
   * Defines the operations used when loading Configurations.  Different implementations load
   * Configurations from the filesystem or from a Jar file.
   */
  private interface DirectoryOperations {

    Collection<String> getSubDirectories(Path configDirectory);

    Collection<String> getFilesInDirectory(String path);

    URL getUrl(String path);
  }

  /**
   * Implements {@link DirectoryOperations} by logging input parameters and operation results of
   * invoking a delegate {@link DirectoryOperations} implementation.
   */
  private static class LoggingDirectoryOperations implements DirectoryOperations {

    private final DirectoryOperations delegate;

    private LoggingDirectoryOperations(DirectoryOperations delegate) {
      this.delegate = delegate;
    }

    @Override
    public Collection<String> getSubDirectories(Path configDirectory) {
      final var cleansedString = Validation.cleanseInputString(configDirectory.toString());
      FileConfigurationRepository.logger.info("Finding subdirectories of {}", cleansedString);

      Collection<String> subdirectories = this.delegate.getSubDirectories(configDirectory);

      if (FileConfigurationRepository.logger.isInfoEnabled()) {
        final var cleansedSubdirString = Validation.cleanseInputString(Arrays.toString(subdirectories.toArray()));
        FileConfigurationRepository.logger
          .info("Found subdirectories {}", cleansedSubdirString);
      }

      return subdirectories;
    }

    @Override
    public Collection<String> getFilesInDirectory(String path) {
      FileConfigurationRepository.logger.info("Loading files from directory {}", path);

      Collection<String> files = this.delegate.getFilesInDirectory(path);

      if (FileConfigurationRepository.logger.isInfoEnabled()) {
        FileConfigurationRepository.logger
          .info("Found files to load {}", Arrays.toString(files.toArray()));
      }

      return files;
    }

    @Override
    public URL getUrl(String path) {
      FileConfigurationRepository.logger.info("Getting URL for path {}", path);

      var url = this.delegate.getUrl(path);
      FileConfigurationRepository.logger.info("URL is {} ", url);

      return url;
    }
  }

  /**
   * Implements {@link DirectoryOperations} using filesystem operations
   */
  private static class FilesystemDirectoryOperations implements DirectoryOperations {

    @Override
    public Collection<String> getSubDirectories(Path configDirectory) {
      try (var subdirectories = Files.list(configDirectory)) {
        return subdirectories.filter(Files::isDirectory)
          .map(Path::toString)
          .collect(Collectors.toList());
      } catch (IOException e) {
        logger.error("Error reading subdirectories: ", e);
        return Collections.emptyList();
      }
    }

    @Override
    public Collection<String> getFilesInDirectory(String path) {
      return Optional.ofNullable(new File(path).list())
        .map(listing ->
          Arrays.stream(listing)
            .map(f -> path + File.separator + f)
            .collect(Collectors.toList())
        ).orElse(List.of());
    }

    @Override
    public URL getUrl(String path) {
      try {
        return new File(path).toURI().toURL();
      } catch (MalformedURLException e) {
        String message = "Could not create URL to file at path: " + path;
        throw new IllegalStateException(message, e);
      }
    }
  }

  /**
   * Implements {@link DirectoryOperations} for files in a jar file.
   */
  private static class ResourcesDirectoryOperations implements DirectoryOperations {

    @Override
    public Collection<String> getSubDirectories(Path configDirectory) {

      String path = ResourcesDirectoryOperations.relativeToJar(configDirectory.toAbsolutePath().toString());

      final var cleansedString = Validation.cleanseInputString(path);
      FileConfigurationRepository.logger.info("Loading all resources in directory {}", cleansedString);

      URL resource = ResourcesDirectoryOperations.getClassLoader().getResource(path);
      FileConfigurationRepository.logger.info("Found resource for directory {}", resource);

      return this.list(path).stream()
        .filter(s -> s.endsWith(File.separator))
        .collect(Collectors.toList());
    }

    @Override
    public Collection<String> getFilesInDirectory(String path) {
      return this.list(path).stream()
        .filter(s -> !s.endsWith(File.separator))
        .collect(Collectors.toList());
    }

    @Override
    public URL getUrl(String path) {
      FileConfigurationRepository.logger.info("Loading file from resources path {}", path);
      return ResourcesDirectoryOperations.getResourceUrl(path);
    }

    /**
     * Find all of the children of the provided directory within a Jar file.  The provided
     * directoryPath and the returned paths are relative to the jar file.
     *
     * @param directoryPath path to a directory, relative to the jar file, not null
     * @return list of paths to children of the provided directoryPath, all paths are relative to
     * the jar file, not null
     */
    private List<String> list(String directoryPath) {

      // If necessary, append File.separator to end of directoryPath
      String rootPath =
        directoryPath + (directoryPath.endsWith(File.separator) ? "" : File.separator);

      // True if the string is a direct child of the rootPath
      Predicate<String> isChild = s -> s.startsWith(rootPath)
        && !rootPath.equals(s)
        && (!s.substring(rootPath.length(), s.length() - 1).contains(File.separator));

      // Find direct children of the root path
      return Optional.ofNullable(ResourcesDirectoryOperations.getResourceUrl(directoryPath))
        .map(URL::getPath)
        .map(p -> p.substring("file:".length(), p.indexOf('!')))
        .map(jarPath -> ResourcesDirectoryOperations.filterJarEntries(jarPath, isChild))
        .orElse(List.of());
    }

    /**
     * Finds names of all entries in the Jar file at the provided jarPath which satisfy the filter
     *
     * @param jarPath path to a jarFile, not null
     * @param filter {@link Predicate} deciding which jar entries are listed
     * @return List of String paths to jar entries passing the filter, not null
     */
    private static List<String> filterJarEntries(String jarPath, Predicate<String> filter) {
      List<String> filenames = new ArrayList<>();

      try (var jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
        jar.entries().asIterator().forEachRemaining(entry -> {
            String entryName = entry.getName();
            if (filter.test(entryName)) {
              Optional.ofNullable(ResourcesDirectoryOperations.getResourceUrl(entryName))
                .map(URL::toString)
                .map(ResourcesDirectoryOperations::relativeToJar)
                .ifPresent(filenames::add);
            }
          }
        );
      } catch (UnsupportedEncodingException e) {
        FileConfigurationRepository.logger.error("Could not decode URL, ", e);
      } catch (IOException e) {
        FileConfigurationRepository.logger.error("Could not access jar, ", e);
      }

      return filenames;
    }

    private static URL getResourceUrl(String path) {
      return ResourcesDirectoryOperations.getClassLoader().getResource(path);
    }

    private static ClassLoader getClassLoader() {
      return Thread.currentThread().getContextClassLoader();
    }

    private static String relativeToJar(String fullPath) {
      return fullPath.substring(fullPath.indexOf('!') + 2);
    }
  }
}
