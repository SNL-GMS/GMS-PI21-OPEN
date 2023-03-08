package gms.shared.frameworks.control;

import com.google.auto.value.AutoValue;
import gms.shared.frameworks.client.generation.ClientGenerator;
import gms.shared.frameworks.configuration.ConfigurationRepository;
import gms.shared.frameworks.configuration.RetryConfig;
import gms.shared.frameworks.configuration.repository.client.ConfigurationConsumerUtility;
import gms.shared.frameworks.systemconfig.SystemConfig;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents initialized versions of the common external dependencies (e.g. PluginRegistry,
 * configuration, logging, etc.) required by a GMS Control. Created by a {@link Builder} which
 * provides default dependency instantiations for any dependencies the client leaves unset.
 */
@AutoValue
public abstract class ControlContext {

  /**
   * Obtain the {@link SystemConfig} from this {@link ControlContext}
   *
   * @return {@link SystemConfig}, not null
   */
  public abstract SystemConfig getSystemConfig();

  /**
   * Obtain the {@link ConfigurationRepository} from this {@link ControlContext}
   *
   * @return {@link ConfigurationRepository}, not null
   */
  public abstract ConfigurationRepository getProcessingConfigurationRepository();

  /**
   * Obtain the {@link ConfigurationConsumerUtility} from this {@link ControlContext}
   *
   * @return {@link ConfigurationConsumerUtility}, not null
   */
  public abstract ConfigurationConsumerUtility getProcessingConfigurationConsumerUtility();

  /**
   * Obtain an {@link ControlContext} {@link Builder} for a control with the provided name
   *
   * @param controlName control name, not null
   * @return {@link Builder}, not null
   * @throws NullPointerException if controlName is null
   */
  public static Builder builder(String controlName) {
    Objects.requireNonNull(controlName, "ControlContext.Builder requires non-null controlName");
    return new AutoValue_ControlContext.Builder().setControlName(controlName);
  }

  /**
   * Constructs a {@link ControlContext} from provided values or defaults as necessary.
   */
  @AutoValue.Builder
  public abstract static class Builder {

    private String controlName;

    /**
     * Set the controlName used in this Builder.
     *
     * @param controlName the name of the control component, not null
     * @return this {@link Builder}
     */
    Builder setControlName(String controlName) {
      this.controlName = Objects.requireNonNull(controlName);
      return this;
    }

    /**
     * Set the {@link SystemConfig} to use in the built {@link ControlContext}
     *
     * @param systemConfig {@link SystemConfig}, not null
     * @return this {@link Builder}
     */
    public abstract Builder systemConfig(SystemConfig systemConfig);

    /**
     * Obtains the {@link SystemConfig} set in this builder if one has been set. Not Optional since
     * a SystemConfig is set when the builder is instantiated.
     *
     * @return {@link SystemConfig} set in this Builder
     */
    abstract Optional<SystemConfig> getSystemConfig();

    /**
     * Set the {@link ConfigurationRepository} to use in the built {@link ControlContext}.
     *
     * @param processingConfigurationRepository {@link ConfigurationRepository}, not null
     * @return this {@link Builder}
     */
    public abstract Builder processingConfigurationRepository(
      ConfigurationRepository processingConfigurationRepository);

    /**
     * Obtains the {@link ConfigurationRepository} set in this builder if one has been set.
     *
     * @return {@link Optional} containing the {@link ConfigurationRepository} set in this Builder
     */
    abstract Optional<ConfigurationRepository> getProcessingConfigurationRepository();

    /**
     * Set the {@link ConfigurationConsumerUtility} to use in the built {@link ControlContext}.
     *
     * @param consumer {@link ConfigurationConsumerUtility}, not null
     * @return this {@link Builder}
     */
    abstract Builder processingConfigurationConsumerUtility(ConfigurationConsumerUtility consumer);

    /**
     * AutoValue generated builder. Called by {@link Builder#build()}.
     *
     * @return {@link ControlContext}, not null
     */
    abstract ControlContext autoBuild();

    /**
     * Obtain the {@link ControlContext} defined by this {@link Builder}. Uses defaults for any
     * ControlContext properties not explicitly set in this Builder.
     *
     * @return {@link ControlContext}, not null
     */
    public ControlContext build() {
      ConfigurationRepository configRepo = getConfigRepoOrMakeDefault();
      var systemConfig = getSystemConfigOrMakeDefault();
      var retryConfig = RetryConfig.create(systemConfig.getValueAsInt("processing-retry-initial-delay"),
        systemConfig.getValueAsInt("processing-retry-max-delay"),
        ChronoUnit.valueOf(systemConfig.getValue("processing-retry-delay-units")),
        systemConfig.getValueAsInt("processing-retry-max-attempts"));

      processingConfigurationConsumerUtility(ConfigurationConsumerUtility.builder(configRepo)
        .selectorCacheExpiration(systemConfig.getValueAsDuration("config-cache-expiration"))
        .retryConfiguration(retryConfig)
        .configurationNamePrefixes(List.of(this.controlName))
        .build());
      return autoBuild();
    }

    private ConfigurationRepository getConfigRepoOrMakeDefault() {
      final Optional<ConfigurationRepository> setConfigRepo = getProcessingConfigurationRepository();
      final ConfigurationRepository configRepo;
      if (setConfigRepo.isEmpty()) {
        configRepo = ClientGenerator.createClient(ConfigurationRepository.class, getSystemConfigOrMakeDefault());
        processingConfigurationRepository(configRepo);
      } else {
        configRepo = setConfigRepo.get();
      }
      return configRepo;
    }

    private SystemConfig getSystemConfigOrMakeDefault() {
      final Optional<SystemConfig> setConfig = getSystemConfig();
      final SystemConfig config;
      if (setConfig.isEmpty()) {
        config = SystemConfig.create(this.controlName);
        systemConfig(config);
      } else {
        config = setConfig.get();
      }
      return config;
    }
  }
}
