package gms.shared.featureprediction.plugin.api.lookuptable;

import com.google.common.primitives.ImmutableDoubleArray;
import gms.shared.plugin.Plugin;
import gms.shared.signaldetection.coi.types.PhaseType;
import java.util.Set;

/**
 * Interface for plugins providing earth model data which varies with PhaseType, source to receiver
 * distance, and source depth.
 *
 * @param <V> the type of the data returned by the plugin
 * @param <U> the units of the data returned by the plugin
 */
public interface EarthModelDepthDistanceLookupTablePlugin<V, U> extends Plugin {

  /**
   * Returns the Units associated with the data values returned by the getValues
   * and getStandardDeviations operations.
   *
   * @return Units of the value data returned by plugin.
   */
  public U getUnits();

  /**
   * Returns the collection of PhaseTypes which have data defined by this plugin.
   *
   * @return set of phase types which have data defined by this plugin.
   */
  public Set<PhaseType> getAvailablePhaseTypes();

  /**
   * Returns an ordered immutable array of depths this model uses to define earth model data for
   * the provided PhaseType. The returned collection is sorted by increasing depth. Depth values
   * have units of kilometers. Returns an empty response if the plugin does not define values for
   * the provided PhaseType.
   *
   * @param phaseType the PhaseType for the requested depths
   * @return immutable ordered array of depths in kilometers.  Returns empty array if values not
   * defined for requested PhaseType
   */
  public ImmutableDoubleArray getDepthsKmForData(PhaseType phaseType);

  /**
   * Returns an ordered immutable array of distances this model uses to define earth model data for
   * the provided PhaseType. The returned collection is sorted by increasing distance. Distance
   * values have units of kilometers. Returns an empty response if the plugin does not define
   * values for the provided PhaseType.
   *
   * @param phaseType the PhaseType for the requested distances
   * @return immutable ordered array of distances in kilometers.  Returns empty array if values not
   * defined for requested PhaseType
   */
  public ImmutableDoubleArray getDistancesDegForData(PhaseType phaseType);

  /**
   * Returns a 2-dimensional ordered collection containing the earth model data values this plugin
   * defines for the provided PhaseType. The returned collection is sorted to match the depth and
   * distance values returned by the getDepthsKmForData and getDistancesDegForData operations,
   * i.e. the value at the first depth index and the first distance index corresponds to the first
   * depth and first distance in the collections returned by getDepthsKmForData and
   * getDistancesDegForData. The collection is indexed first by depth and then by distance,
   * i.e. values[depthIndex][distanceIndex]. This operation returns an empty response if the plugin
   * does not define values for the provided PhaseType.
   *
   * @param phaseType the PhaseType for the requested values
   * @return a 2D ordered collection of values this plugin defines for the provided PhaseType.
   */
  public V getValues(PhaseType phaseType);

  /**
   * Returns an ordered immutable array of depths this model uses to define earth model data value
   * standard deviations for the provided PhaseType. The returned collection is sorted by
   * increasing depth. Depth values have units of kilometers. Returns an empty response if the
   * plugin does not define depth values for standard deviation for the provided PhaseType.
   *
   * @param phaseType the PhaseType for the requested depths
   * @return immutable ordered array of depths in kilometers.  Returns empty array if depths not
   * defined for requested PhaseType
   */
  public ImmutableDoubleArray getDepthsKmForStandardDeviations(PhaseType phaseType);

  /**
   * Returns an ordered immutable array of distances this model uses to define earth model data value
   * standard deviations for the provided PhaseType. The returned collection is sorted by
   * increasing distance. Distance values have units of kilometers. Returns an empty response if the
   * plugin does not define distance values for standard deviation for the provided PhaseType.
   *
   * @param phaseType the PhaseType for the requested distances
   * @return immutable ordered array of distances in kilometers.  Returns empty array if distances
   * not defined for requested PhaseType
   */
  public ImmutableDoubleArray getDistancesDegForStandardDeviations(PhaseType phaseType);

  /**
   * Returns a 2-dimensional ordered collection containing the earth model data value standard
   * deviations this plugin defines for the provided PhaseType. The returned collection is sorted to
   * match the depth and distance values returned by the getDepthsKmForStandardDeviations and
   * getDistancesDegForStandardDeviations operations, i.e. the value at the first depth index and
   * the first distance index corresponds to the first depth and first distance in the collections
   * returned by getDepthsKmForStandardDeviations and getDistancesDegForStandardDeviations.
   * The collection is indexed first by depth and then by distance,
   * i.e. standardDeviations[depthIndex][distanceIndex]. This operation returns an empty response
   * if the plugin does not define standard deviations for the provided PhaseType.
   *
   * @param phaseType the PhaseType for the requested standard deviations
   * @return a 2D ordered collection of standard deviations this plugin defines for the provided PhaseType.
   */
  public V getStandardDeviations(PhaseType phaseType);

}
