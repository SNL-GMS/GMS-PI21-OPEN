package gms.shared.featureprediction.plugin.api.lookuptable;

import gms.shared.featureprediction.utilities.view.Immutable2dArray;
import gms.shared.stationdefinition.coi.utils.Units;
import java.time.Duration;

/**
 * Interface specializing the EarthModelDepthDistanceLookupTablePlugin for plugins providing depth,
 * distance, and PhaseType dependent travel time, and travel time standard deviation.
 */
public interface TravelTimeDepthDistanceLookupTablePlugin
  extends EarthModelDepthDistanceLookupTablePlugin<Immutable2dArray<Duration>, Units> {
}
