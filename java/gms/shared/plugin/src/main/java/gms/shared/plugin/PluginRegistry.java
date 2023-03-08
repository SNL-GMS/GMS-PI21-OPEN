package gms.shared.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * PluginRegistry creates a map of all the classes that implement the global Plugin interface so
 * that a user configured Plugin can be loaded by services/apps/etc when needed.  The Plugins are
 * scanned by Spring upon compile time and so nothing new can be added without re-compiling but any
 * implementations that are in the code base can be configured to be the active algorithm used.
 */

@Component
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PluginRegistry {

  // TODO: Address the below note
  /*5-26-2022
  Note that given the desire to have a registry in the FeaturePredictor class that has a listing
  of plugins that implement FeaturePredictorPlugin which in the case of BicubicSplineFeaturePredictor
  then has a registry of plugins that implement TravelTimeLookupTable is creating a cycle of dependencies
  that Spring is set to allow but still cannot resolve.  There is something odd with how Spring is injecting
  classes into the maps as for the case of the maps in PluginRegistryTest all the plugins are being
  injected and not just classes that match the interface that is specified in the map
  */
  private Map<String, Plugin> mappedTypes;

  @Autowired
  @Lazy
  @Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public void setMappedTypes(Map<String, Plugin> mappedTypes) {
    this.mappedTypes = mappedTypes;
  }

  /**
   * Gets a plugin by names and required type.
   *
   * @param name the name of the plugin
   * @param requiredType the interface the plugin implements or the plugin class itself
   * @return plugin of that type; an exception is thrown if there is no plugin by the requested name
   * or if the found plugin doesn't have the requested type.
   * @throws NullPointerException if name or requiredType are null
   * @throws IllegalArgumentException if errors on retrieving or casting the plugin
   */
  public <T extends Plugin> Optional<T> getPlugin(String name, Class<T> requiredType) {
    Objects.requireNonNull(name, "Cannot get plugin from null name");
    Objects.requireNonNull(requiredType, "Cannot get plugin of null type");
    return Optional.ofNullable(requiredType.cast(this.mappedTypes.get(name)));
  }

}


