package gms.shared.frameworks.configuration.repository.client;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.Stream;

import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gms.shared.frameworks.configuration.Configuration;
import gms.shared.frameworks.configuration.ConfigurationReference;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.ConfigurationResolver;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.Selector;
import gms.shared.frameworks.osd.coi.FieldMapUtilities;
import gms.shared.frameworks.utilities.Validation;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

/**
 * Utility used by client applications to obtain parameters from {@link Configuration}. Resolves
 * parameters to either a field map (map of String to Object) or an instance of a parameters class.
 * Uses a {@link ConfigurationRepository} implementation to retrieve the {@link Configuration}s to
 * resolve.
 */
public class ConfigurationConsumerUtility {

  private static final Logger logger = LoggerFactory.getLogger(ConfigurationConsumerUtility.class);

  private static final Duration DEFAULT_CACHE_EXPIRATION = Duration.ofDays(1);

  private final ConfigurationRepository configurationRepository;

  private final RetryConfig retryConfig;

  private final Map<String, ConfigurationSelectorCache> configurationCache;

  private final Duration selectorCacheExpiration;
  
  private final List<String> configurationNamePrefixes;

  private ConfigurationConsumerUtility(ConfigurationRepository configurationRepository,
    Duration selectorCacheExpiration, RetryConfig retryConfig, List<String> configurationNamePrefixes) {
    this.configurationRepository = configurationRepository;
    this.configurationCache = new HashMap<>();
    this.selectorCacheExpiration = selectorCacheExpiration;
    this.retryConfig = retryConfig;
    this.configurationNamePrefixes = configurationNamePrefixes;
  }

  /**
   * {@link ConfigurationConsumerUtility} builder
   */
  public static class Builder {

    private final ConfigurationRepository configurationRepository;
    private Duration selectorCacheExpiration;
    private List<String> configurationNamePrefixes = List.of();
    private RetryConfig retryConfiguration;

    private Builder(ConfigurationRepository configurationRepository) {
      this.configurationRepository = configurationRepository;
      this.selectorCacheExpiration = DEFAULT_CACHE_EXPIRATION;
    }

    public ConfigurationRepository getConfigurationRepository() {
      return this.configurationRepository;
    }

    /**
     * Sets the configurationNamePrefixes of the {@link Configuration}s the {@link
     * ConfigurationConsumerUtility} will use.
     *
     * @param configurationNamePrefixes name prefixes (key prefixes) of the {@link Configuration}s
     * the ConfigurationConsumerUtility will use, not null
     * @return this {@link Builder}, not null
     * @throws NullPointerException if configurationNamePrefixes is null
     */
    public Builder configurationNamePrefixes(Collection<String> configurationNamePrefixes) {
      Objects
        .requireNonNull(configurationNamePrefixes, "Requires non-null configurationNamePrefixes");
      this.configurationNamePrefixes = new ArrayList<>(configurationNamePrefixes);
      return this;
    }

    public List<String> getConfigurationNamePrefixes() {
      return this.configurationNamePrefixes;
    }

    public Builder retryConfiguration(RetryConfig retryConfiguration) {
      Objects
        .requireNonNull(retryConfiguration, "Requires non-null retryConfiguration");
      this.retryConfiguration = retryConfiguration;
      return this;
    }

    public RetryConfig getRetryConfiguration() {
      return this.retryConfiguration;
    }

    public Builder selectorCacheExpiration(Duration selectorCacheExpiration) {
      Objects.requireNonNull(selectorCacheExpiration, "Requires non-null cache expiration Duration");
      this.selectorCacheExpiration = selectorCacheExpiration;
      return this;
    }

    public Duration getSelectorCacheExpiration() {
      return this.selectorCacheExpiration;
    }

    /**
     * Obtains a {@link ConfigurationConsumerUtility} capable of resolving parameters from {@link
     * Configuration}s with the {@link Builder#configurationNamePrefixes}.
     *
     * @return {@link ConfigurationConsumerUtility}, not null
     * @throws IllegalStateException if the {@link ConfigurationRepository} does not have an entry
     * for any of the configurationNamePrefixes.
     */
    public ConfigurationConsumerUtility build() {

      // Add the key prefix used to load global configuration defaults to configurationNamePrefixes
      final List<String> configurationNamePrefixesWithGlobal = Stream.concat(
          configurationNamePrefixes.stream(),
          Stream.of(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX))
        .map(Validation::cleanseInputString)
        .collect(toList());

      logger.info("Creating ConfigurationConsumerUtility for Configurations named : {}",
        configurationNamePrefixesWithGlobal);

      // Initialize ConfigurationConsumerUtility with the Configuration associated with each provided key
      Collection<Configuration> initialConfigurations = loadConfigurations(
        configurationRepository, configurationNamePrefixesWithGlobal, retryConfiguration);

      logger.info("Loaded configurations: {}", initialConfigurations.stream()
        .map(Configuration::getName)
        .collect(Collectors.toList()));

      var configurationConsumerUtility =
        new ConfigurationConsumerUtility(configurationRepository, selectorCacheExpiration, retryConfiguration, configurationNamePrefixes);
      configurationConsumerUtility.addConfigurations(initialConfigurations);

      return configurationConsumerUtility;
    }
  }

  /**
   * Obtains a new {@link Builder} for a {@link ConfigurationConsumerUtility} that will use the
   * provided {@link ConfigurationRepository}
   *
   * @param configurationRepository {@link ConfigurationRepository}, not null
   * @return {@link Builder}, not null
   * @throws NullPointerException if configurationRepository is null
   */
  public static Builder builder(ConfigurationRepository configurationRepository) {
    Objects.requireNonNull(configurationRepository, "Requires non-null ConfigurationRepository");
    return new Builder(configurationRepository);
  }
  
  /**
   * Obtains a new {@link Builder} based on the current {@link ConfigurationConsumerUtility}
   * @return {@link Builder}, not null
   */
  public ConfigurationConsumerUtility.Builder toBuilder() {
    return builder(configurationRepository)
      .configurationNamePrefixes(configurationNamePrefixes)
      .retryConfiguration(retryConfig)
      .selectorCacheExpiration(selectorCacheExpiration);
  }

  /**
   * Updates this ConfigurationConsumerUtility to be able to resolve parameters from {@link
   * Configuration}s with the provided configurationNamePrefixes.  Uses the {@link
   * ConfigurationRepository} provided during construction to load the {@link Configuration}s.
   *
   * @param configurationNamePrefixes name prefixes (key prefixes) of the new {@link Configuration}s
   * for the ConfigurationConsumerUtility to use, not null
   * @return A collection of the configurations loaded
   * @throws NullPointerException if configurationRepository or configurationNamePrefixes are null
   * @throws IllegalStateException if the {@link ConfigurationRepository} does not have an entry for
   * any of the configurationNamePrefixes..
   */
  Collection<Configuration> loadConfigurations(List<String> configurationNamePrefixes) {
    Objects
      .requireNonNull(configurationNamePrefixes, "Requires non-null configurationNamePrefixes");

    logger.info("Loading into ConfigurationConsumerUtility Configurations named : {}",
      configurationNamePrefixes);

    // Workaround to circumvent the race-condition of failing to load global configuration in ConfigurationConsumerUtility.Builder.build()
    // When all services are migrated away from the Frameworks Component pattern that relies on resolving config even when the service doesn't use it,
    // then we can move towards a more permanent and robust solution of ensuring this utility is not successfully built without resolving all requested
    // configuration successfully
    List<String> configurationNamePrefixesWithGlobal = Stream.concat(
      configurationNamePrefixes.stream(),
      Stream.of(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)
    ).collect(toList());

    Collection<Configuration> configCollection = loadConfigurations(this.configurationRepository,
      configurationNamePrefixesWithGlobal, retryConfig);

    addConfigurations(configCollection);

    return configCollection;
  }

  /**
   * Adds the provided {@link Configuration}s to {@link ConfigurationConsumerUtility#configurationCache}.
   * Uses each {@link Configuration#getName()} for the keys.  Does not overwrite existing mappings.
   * <p>
   * Also loads the global default Configurations using key prefix {@link
   * GlobalConfigurationReferenceResolver#REFERENCED_CONFIGURATION_KEY_PREFIX}
   *
   * @param configurations Configurations to add, not null
   */
  private void addConfigurations(Collection<Configuration> configurations) {

    // Create global config map from global and non global configs
    LinkedList<String> finalReferenceList = new LinkedList<>();
    Map<String, List<String>> referenceMap = new HashMap<>();

    // create and sort the global config map
    Map<String, Configuration> globalConfigMap = configurations.stream()
      .collect(Collectors.toMap(Configuration::getName,
        Function.identity(),
        (oldVal, newVal) -> oldVal));

    // find all configuration references in the global config map
    globalConfigMap.forEach((key, config) -> {
      List<String> currentReferenceList = new ArrayList<>();
      config.getConfigurationOptions().forEach(opt -> findConfigurationReferences(
        opt.getParameters(), currentReferenceList));
      if (!currentReferenceList.isEmpty()) {
        referenceMap.put(key, currentReferenceList);
      }
    });

    // loop through reference map and create list of references
    var referenceKeys = referenceMap.keySet();
    for (var key : referenceKeys) {
      // check if current config has already been found as a nested reference
      if (!finalReferenceList.contains(key)) {
        // create all nested references for current config
        createReferences(key, referenceMap, finalReferenceList);
      }
    }

    // create list of all reference configs and load them prior to other global configs
    List<Configuration> referenceConfigs = new ArrayList<>();
    for (String refString : finalReferenceList) {
      referenceConfigs.add(globalConfigMap.remove(refString));
    }

    // loop through the reference configs and create cache
    referenceConfigs.stream()
      .map(config -> GlobalConfigurationReferenceResolver.resolve(this.configurationCache, config))
      .forEach(config -> configurationCache.put(config.getName(),
        ConfigurationSelectorGuavaCache.create(config, selectorCacheExpiration)));

    // load global configs after references have been resolved
    globalConfigMap.values().stream()
      .map(config -> GlobalConfigurationReferenceResolver.resolve(this.configurationCache, config))
      .forEach(config -> configurationCache.put(config.getName(),
        ConfigurationSelectorGuavaCache.create(config, selectorCacheExpiration)));
  }

  /**
   * Find and create references for the current configuration key
    * @param key the key for the parent config
   * @param referenceMap map of references for configurations
   * @param finalReferenceList final reference list containing all references
   */
  private void createReferences(String key, Map<String, List<String>> referenceMap,
    LinkedList<String> finalReferenceList) {

    // get the dependency list from the map
    var currentReferenceList = referenceMap.get(key) != null
      ? referenceMap.get(key) : List.<String>of();
    for (var ref : currentReferenceList) {
      // if current reference has been found then continue
      if (!finalReferenceList.contains(ref)) {
        // recurse the nested reference
        createReferences(ref, referenceMap, finalReferenceList);
        addReference(ref, finalReferenceList);
      }
    }
  }

  /**
   * Add configuration reference to the reference list if it does not exist
   * @param ref reference to add to the configuration reference list
   * @param referenceList list of configuration references
   */
  private void addReference(String ref, LinkedList<String> referenceList) {
    if (!referenceList.contains(ref)) {
      referenceList.add(ref);
    }
  }

  /**
   * Finds all configuration references and populates reference stack to maintain config order
   *
   * @param parameters HashMap of config parameters
   * @param currentReferenceList List of reference strings for current config
   */
  private void findConfigurationReferences(Map<String, Object> parameters,
    List<String> currentReferenceList) {
    Set<String> keys = new HashSet<>(parameters.keySet());

    // add keys for parameters that contain references
    var queueKeys = keys.stream()
      .filter(ConfigurationReference::isConfigurationReferenceKey)
      .map(key -> key.replace(ConfigurationReference.REF_COMMAND, ""))
      .filter(key -> !(currentReferenceList.contains(key)))
      .collect(toList());
    currentReferenceList.addAll(queueKeys);

    // check for nested config references
    checkNestedConfigurationReferences(parameters, currentReferenceList);
  }

  /**
   * Check for nested configuration references in the config parameters
   *
   * @param parameters Configuration parameters
   * @param currentReferenceList List of reference strings for current config
   */
  private void checkNestedConfigurationReferences(Map<String, Object> parameters,
    List<String> currentReferenceList) {
    // check for any nested references in current parameters
    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
      Object paramVal = entry.getValue();

      if (paramVal instanceof Collection) {
        for (Object nestedVal : (Collection) paramVal) {
          // check if the nested val object contains a reference
          if (nestedVal instanceof Map) {
            findNestedConfigurationReferences(nestedVal, currentReferenceList);
          }
        }
      } else if (paramVal instanceof Map) {
        findNestedConfigurationReferences(paramVal, currentReferenceList);
      }
    }
  }

  /**
   * Resolve all nested configurations and enqueue
   *
   * @param nestedConfig Nested config object
   * @param referenceList Deque for storing references
   */
  private void findNestedConfigurationReferences(Object nestedConfig,
    List<String> referenceList) {
    @SuppressWarnings("unchecked") final Map<String, Object> nestedConfigMap = (Map<String, Object>) nestedConfig;
    findConfigurationReferences(nestedConfigMap, referenceList);
  }

  /**
   * Loads {@link Configuration}s with the provided name prefixes from the provided {@link
   * ConfigurationRepository}.
   *
   * @param configurationRepository {@link ConfigurationRepository} implementation providing the
   * Configurations, not null
   * @param configurationNamePrefixes name prefixes (key prefixes) of the {@link Configuration}s to
   * load, not null
   * @return Collection of {@link Configuration}, not null
   */
  private static Collection<Configuration> loadConfigurations(
    ConfigurationRepository configurationRepository,
    Collection<String> configurationNamePrefixes, RetryConfig retryConfig) {

    // Load all configurations while keeping track of keys not found in the ConfigurationRepository
    try {
      List<String> missingConfigKeys = new ArrayList<>();
      List<String> missingGlobalKeys = new ArrayList<>();
      List<Configuration> configurations = configurationNamePrefixes.stream()
        .distinct()
        .map(prefix -> {
          List<Configuration> configsFromPrefix = loadConfiguration(configurationRepository, prefix, retryConfig);
          if (configsFromPrefix.isEmpty()) {
            if (!prefix.startsWith(GlobalConfigurationReferenceResolver.REFERENCED_CONFIGURATION_KEY_PREFIX)) {

              missingConfigKeys.add(prefix);
            } else {
              missingGlobalKeys.add(prefix);
            }
          }
          return configsFromPrefix;
        })
        .flatMap(List::stream)
        .collect(toList());

      // Throw an exception if any of the requested keys are missing
      if (!missingConfigKeys.isEmpty()) {
        var message = String.format("No Configuration(s) found for key prefix(es) %s",
          Arrays.toString(missingConfigKeys.toArray()));

        logger.warn(message);
      }

      if (!missingGlobalKeys.isEmpty()) {
        var message = String.format("No Configuration(s) found for global prefix(es) %s",
          Arrays.toString(missingGlobalKeys.toArray()));

        logger.warn(message);
      }
      return configurations;
    } catch (Exception ex) {
      throw new IllegalStateException("Error loading configurations", ex);
    }
  }

  /**
   * Loads a list of {@link Configuration} with the provided name prefix from the {@link
   * ConfigurationRepository}.
   *
   * @param configurationRepository {@link ConfigurationRepository} implementation providing the
   * Configuration, not null
   * @param configurationNamePrefix names prefix (key prefix) of the {@link Configuration}s to load,
   * not null
   * @return list of {@link Configuration}, not null
   */
  private static List<Configuration> loadConfiguration(ConfigurationRepository configurationRepository,
    String configurationNamePrefix, RetryConfig retryConfig) {

    final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
      .withBackoff(retryConfig.getInitialDelay(), retryConfig.getMaxDelay(), retryConfig.getDelayUnits())
      .withMaxAttempts(retryConfig.getMaxAttempts())
      .handle(List.of(NoResultException.class, ExecutionException.class, IllegalStateException.class,
        InterruptedException.class))
      .onFailedAttempt(e -> logger.warn("Error loading configuration {} (attempt {} of {})", configurationNamePrefix,
        e.getAttemptCount(), retryConfig.getMaxAttempts(),
        e.getLastFailure()));

    return new ArrayList<>(
      Failsafe.with(retryPolicy).get(() -> configurationRepository.getKeyRange(configurationNamePrefix)));
  }

  /**
   * Uses the provided {@link Selector}s to resolve parameters from the {@link Configuration} with
   * the provided name.  Returns the resolved parameters as an instance of the provided
   * parametersClass
   *
   * @param configurationName name of the Configuration to resolve
   * @param selectors {@link Selector}s describing how to resolve the Configuration
   * @param parametersClass class type of the resolved parameters, not null
   * @param <T> type of the parametersClass
   * @return Instance of T (the parametersClass) containing the resolved parameters, not null
   * @throws NullPointerException if configurationName, selectors, or parametersClass are null
   * @throws IllegalArgumentException if this ConfigurationConsumerUtility does not have a
   * Configuration with the provided name
   * @throws IllegalArgumentException if the resolved parameters cannot be used to construct an
   * instance of T (the parametersClass)
   * @see ConfigurationResolver#resolve(Configuration, List) for details of the resolution
   * algorithm.
   */
  public <T> T resolve(String configurationName, List<Selector> selectors,
    Class<T> parametersClass) {

    Objects.requireNonNull(parametersClass, "Cannot resolve Configuration to null parametersClass");

    // Resolve and construct parametersClass instance.
    // resolve() call is not inlined in SerializationUtility.fromFieldMap call since both calls
    // produce IllegalArgumentException but only the SerializationUtility's exception is caught and
    // rethrown.
    final Map<String, Object> resolvedFieldMap = resolve(configurationName, selectors);
    try {
      return FieldMapUtilities.fromFieldMap(resolvedFieldMap, parametersClass);
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException(
        "Resolved Configuration is not a valid instance of " + parametersClass.getCanonicalName(),
        e);
    }
  }

  /**
   * Uses the provided {@link Selector}s to resolve parameters from the {@link Configuration} with
   * the provided name.  Returns the resolved parameters in a field map.
   *
   * @param configurationName name of the Configuration to resolve
   * @param selectors {@link Selector}s describing how to resolve the Configuration
   * @return field map containing the resolved parameters, not null
   * @throws NullPointerException if configurationName or selectors are null
   * @throws IllegalArgumentException if this ConfigurationConsumerUtility does not have a
   * Configuration with the provided name
   * @see ConfigurationResolver#resolve(Configuration, List) for details of the resolution
   * algorithm.
   */
  public Map<String, Object> resolve(String configurationName, List<Selector> selectors) {
    Objects.requireNonNull(configurationName,
      "Cannot resolve Configuration for null configurationName");
    Objects.requireNonNull(selectors, "Cannot resolve Configuration for null selectors");

    final RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
      .withBackoff(retryConfig.getInitialDelay(), retryConfig.getMaxDelay(), retryConfig.getDelayUnits())
      .withMaxAttempts(retryConfig.getMaxAttempts())
      .handleResult(List.of())
      .onFailedAttempt(e -> logger.warn(
        String.format("No config found for %s during config resolution, trying again as may be in a race with loading",
          configurationName),
        e.getLastFailure()));

    ConfigurationSelectorCache configuration = configurationCache.get(configurationName);
    if (configuration == null) {
      Failsafe.with(retryPolicy).get(() -> loadConfigurations(Collections.singletonList(configurationName)));
    }

    return Optional.ofNullable(configurationCache.get(configurationName))
      .orElseThrow(() -> new IllegalArgumentException(
        "No Configuration named " + configurationName
          + " is in this ConfigurationConsumerUtility"))
      .resolveFieldMap(selectors);
  }

  protected Duration getSelectorCacheExpiration() {
    return selectorCacheExpiration;
  }

}
