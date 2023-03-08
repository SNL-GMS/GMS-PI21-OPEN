package gms.core.ui.processing.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.frameworks.configuration.Selector;

import java.util.Collections;
import java.util.List;

/**
 * Data transfer object containing a configuration name and a list of selectors.
 */
@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ConfigQuery {

  /**
   * Gets the configuration name
   *
   * @return the configuration name
   */
  @JsonProperty("configName")
  public abstract String getConfigurationName();

  /**
   * Gets the selectors
   *
   * @return the selectors
   */
  public abstract List<Selector> getSelectors();

  /**
   * Create a {@link ConfigQuery} from all parameters
   *
   * @param configName the name of the configuration
   * @param selectors the selectors
   * @return a {@link ConfigQuery}
   */
  @JsonCreator
  public static ConfigQuery from(
    @JsonProperty("configName") String configName,
    @JsonProperty("selectors") List<Selector> selectors) {
    return new AutoValue_ConfigQuery(
      configName, Collections.unmodifiableList(selectors));
  }
}
