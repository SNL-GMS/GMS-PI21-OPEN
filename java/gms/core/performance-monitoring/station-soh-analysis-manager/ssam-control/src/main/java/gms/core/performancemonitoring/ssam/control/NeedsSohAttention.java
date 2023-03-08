package gms.core.performancemonitoring.ssam.control;

import gms.shared.frameworks.osd.coi.signaldetection.Station;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * This is a class to indicate which stations require operator attention due to their
 * state of health. An instance of this class is created on startup by the
 * {@link ReactiveStationSohAnalysisManager}. Instances of this class are immutable.
 */
public class NeedsSohAttention {

  private final Instant creationTime;
  private final Set<String> stationNames;

  /**
   * Constructor. Private, because of architectural guidance.
   *
   * @param creationTime the creation time of the object, which cannot be null.
   * @param stationNames the names of the stations that require soh attention. This may be
   * empty, but not null.
   */
  private NeedsSohAttention(Instant creationTime, Collection<String> stationNames) {
    this.creationTime = creationTime;
    this.stationNames = new LinkedHashSet<>(stationNames);
  }

  /**
   * Static factory method.
   *
   * @param creationTime the instant of creation. May not be null.
   * @param stationNames the names of the stations that require soh attention. This may be empty
   * or null.
   * @return a new instance.
   */
  public static NeedsSohAttention from(Instant creationTime, Collection<String> stationNames) {
    Objects.requireNonNull(creationTime, "creationTime is required");
    return new NeedsSohAttention(creationTime, stationNames != null ? stationNames :
      Collections.emptyList());
  }

  /**
   * Get the creation instant for this object.
   *
   * @return
   */
  public Instant getCreationTime() {
    return creationTime;
  }

  /**
   * Get a list of the names of the stations that require SOH attention.
   *
   * @return
   */
  public List<String> getStationNames() {
    return new ArrayList<>(stationNames);
  }

  /**
   * Does the station require SOH attention?
   *
   * @param station the station, which should not be null.
   * @return true if the name of the station is in the list of station names.
   */
  public boolean stationNeedsSohAttention(Station station) {
    return stationNeedsSohAttention(station.getName());
  }

  /**
   * Does the station require SOH attention?
   *
   * @param stationName the name of the station, which should not be null.
   * @return true if the name of the station is in the list of station names.
   */
  public boolean stationNeedsSohAttention(String stationName) {
    return stationNames.contains(stationName);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NeedsSohAttention that = (NeedsSohAttention) o;
    return Objects.equals(creationTime, that.creationTime) &&
      Objects.equals(stationNames, that.stationNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(creationTime, stationNames);
  }
}
