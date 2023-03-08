package gms.shared.featureprediction.plugin.lookuptable.traveltime;

import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.featureprediction.plugin.api.lookuptable.TravelTimeDepthDistanceLookupTablePlugin;
import gms.shared.featureprediction.utilities.view.Immutable2dArray;
import gms.shared.featureprediction.utilities.view.TravelTimeLookupView;
import gms.shared.featureprediction.utilities.view.TravelTimeLookupViewTransformer;
import gms.shared.signaldetection.coi.types.PhaseType;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.utilities.filestore.FileStore;

import java.time.Duration;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Plugin for travel time lookup tables.
 */
public abstract class TravelTimeLookupTable implements TravelTimeDepthDistanceLookupTablePlugin {

  private static final Logger logger = LoggerFactory.getLogger(TravelTimeLookupTable.class);

  private final String errorMessageUninitializedPlugin =
    String.format("Method called on uninitialized plugin, %s.", getName());

  private final FileStore fileStore;
  private final TravelTimeLookupTableConfiguration configuration;

  // maps the PhaseType to the lookup table for that PhaseType
  private Map<PhaseType, TravelTimeLookupView> viewMap;
  private boolean isInitialized = false;

  protected TravelTimeLookupTable(
    FileStore fileStore,
    TravelTimeLookupTableConfiguration configuration) {

    Validate.notNull(fileStore);
    Validate.notNull(configuration);

    this.fileStore = fileStore;
    this.configuration = configuration;
  }

  public void initialize() {

    Map<PhaseType, TravelTimeLookupView> tmpMap = new EnumMap<>(PhaseType.class);

    logger.info("Initializing data for bucket/key {}, table class name {}",
      configuration.getTravelTimeLookupTableDefinition().getFileDescriptor(), this.getClass().getCanonicalName());

    fileStore
      .findByKeyPrefix(
        configuration.getTravelTimeLookupTableDefinition().getFileDescriptor(),
        new TravelTimeLookupViewTransformer())
      .values()
      .forEach(v -> {
        if (v.getPhase() != PhaseType.UNKNOWN) {
          logger.info("Found a table for known phase type {}", v.getPhase());
          tmpMap.put(v.getPhase(), v);
        } else {
          logger.info("Found a table for an unknown phase type {}", v.getRawPhaseString());
        }
      });

    viewMap = Collections.unmodifiableMap(tmpMap);

    isInitialized = true;
  }

  public Units getUnits() {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    return viewMap.values().iterator().next().getTravelTimeUnits();
  }

  public Set<PhaseType> getAvailablePhaseTypes() {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    return viewMap.keySet();
  }

  private void validatePhaseType(PhaseType phaseType) {
    if (!viewMap.containsKey(phaseType)) {
      throw new IllegalArgumentException(
        String.format("Invalid PhaseType arg, %s, in plugin, %s.", phaseType, getName()));
    }
  }

  public ImmutableDoubleArray getDepthsKmForData(PhaseType phaseType) {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    validatePhaseType(phaseType);
    return viewMap.get(phaseType).getDepths();
  }

  public ImmutableDoubleArray getDistancesDegForData(PhaseType phaseType) {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    validatePhaseType(phaseType);
    return viewMap.get(phaseType).getDistances();
  }

  public Immutable2dArray<Duration> getValues(PhaseType phaseType) {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    validatePhaseType(phaseType);
    return viewMap.get(phaseType).getTravelTimes();
  }

  public ImmutableDoubleArray getDepthsKmForStandardDeviations(PhaseType phaseType) {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    validatePhaseType(phaseType);
    return viewMap.get(phaseType).getModelingErrorDepths();
  }

  public ImmutableDoubleArray getDistancesDegForStandardDeviations(PhaseType phaseType) {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    validatePhaseType(phaseType);
    return viewMap.get(phaseType).getModelingErrorDistances();
  }

  public Immutable2dArray<Duration> getStandardDeviations(PhaseType phaseType) {
    Validate.isTrue(isInitialized, errorMessageUninitializedPlugin);
    validatePhaseType(phaseType);
    return viewMap.get(phaseType).getModelingErrors();
  }

}
