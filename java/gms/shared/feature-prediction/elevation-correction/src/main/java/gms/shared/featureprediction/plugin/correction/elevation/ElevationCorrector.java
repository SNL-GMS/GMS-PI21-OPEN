package gms.shared.featureprediction.plugin.correction.elevation;

import gms.shared.event.coi.featureprediction.FeaturePredictionComponent;
import gms.shared.event.coi.featureprediction.FeaturePredictionComponentType;
import gms.shared.featureprediction.plugin.api.correction.elevation.mediumvelocity.MediumVelocityEarthModelPlugin;
import gms.shared.plugin.PluginRegistry;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.signaldetection.coi.values.DurationValue;
import gms.shared.stationdefinition.coi.channel.Location;
import java.time.Duration;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Springified utility for calculating arrival time corrections based on elevation.
 */
@Component
@ComponentScan(
  basePackages = {
    "gms.shared.featureprediction.utilities.elevationcorrector.plugins"
  }
)
public class ElevationCorrector {

  private PluginRegistry pluginRegistry;

  /**
   * Set the plugin registry. This is a setter (instead of an Autowired field) so that the Lazy annotation can
   * be used.
   * @param pluginRegistry Plugin registry to use.
   */
  @Autowired
  @Lazy
  public void setPluginRegistry(PluginRegistry pluginRegistry) {
    this.pluginRegistry = pluginRegistry;
  }

  @Autowired
  private ElevationCorrectorConfiguration elevationCorrectorConfiguration;

  /**
   * Initialize the utility. Called by spring after construction.
   */
  @PostConstruct
  public void init() {
    elevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition()
      .getMediumVelocityEarthModelPluginNameByModelNameMap()
      .forEach((model, pluginName) -> pluginRegistry.getPlugin(
        pluginName, MediumVelocityEarthModelPlugin.class
      ).orElseThrow(() ->
        new IllegalStateException("A plugin named " + pluginName
          + " is configured for earth model " + model + ", but no plugin was found")).initialize());
  }

  /**
   * Calculate an elevation-based arrival time correction
   *
   * @param mediumVelocityEarthModel Which medium velocity model to use.
   * @param receiverLocation Location of the reciever
   * @param horizontalSlowness Slowness used in calculation
   * @param phaseType Phase to calculate for
   * @return A FeaturePredictionComponent which contains the correction, in the form of a duration to be added to the
   * arrival time.
   */
  public Optional<FeaturePredictionComponent<DurationValue>> correct(
    String mediumVelocityEarthModel,
    Location receiverLocation,
    double horizontalSlowness,
    PhaseType phaseType
  ) {

    // Cant get a medium velocity if we don't have P or S final phase.
    if (phaseType.getFinalPhase() != PhaseType.P && phaseType.getFinalPhase() != PhaseType.S) {
      return Optional.empty();
    }

    var stationVelocityOptional = Optional.ofNullable(elevationCorrectorConfiguration.getCurrentElevationCorrectorDefinition()
      .getPluginNameForEarthModel(mediumVelocityEarthModel))
      .map(pluginName -> pluginRegistry.getPlugin(pluginName, MediumVelocityEarthModelPlugin.class))
      .flatMap(mediumVelocityEarthModelPlugin -> mediumVelocityEarthModelPlugin)
      .map(plugin -> plugin.getValue(phaseType.getFinalPhase(), receiverLocation));

    if (stationVelocityOptional.isEmpty()) {
      return Optional.empty();
    }

    double stationVelocity = stationVelocityOptional.get();

    var x = horizontalSlowness * stationVelocity / 111.949;

    var rawCorrectionInSeconds = (receiverLocation.getElevationKm() / stationVelocity) * Math.sqrt(1 - x * x);

    return Optional.of(FeaturePredictionComponent.from(
      DurationValue.from(
        Duration.ofNanos((long) (1_000_000_000 * rawCorrectionInSeconds)),
        null
      ),
      false,
      FeaturePredictionComponentType.ELEVATION_CORRECTION
    ));
  }
}
