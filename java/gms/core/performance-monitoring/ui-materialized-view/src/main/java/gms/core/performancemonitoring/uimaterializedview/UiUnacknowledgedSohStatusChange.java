package gms.core.performancemonitoring.uimaterializedview;

import gms.shared.frameworks.osd.coi.soh.quieting.SohStatusChange;
import gms.shared.frameworks.osd.coi.soh.quieting.UnacknowledgedSohStatusChange;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Ui Wrapper class to protect against duplicate SohStatusChanges for an Unacknowledged Station
 */
class UiUnacknowledgedSohStatusChange {

  // Map where the channel and soh monitor type make a unique key
  private final Map<String, SohStatusChange> statusChangeMap;
  private final String station;

  /* Constructor creates entry with no status changes
   * @param station name
   */
  public UiUnacknowledgedSohStatusChange(String station) {
    this.station = station;
    this.statusChangeMap = new HashMap<>();
  }

  /**
   * Constructor to create from the COI object. Used at initialization of
   * SohQuietAndUnacknowledgedCacheManager from the DB on startup
   *
   * @param unackSohChanges SohStatusChangeEvent
   */
  public UiUnacknowledgedSohStatusChange(UnacknowledgedSohStatusChange unackSohChanges) {
    this.station = unackSohChanges.getStation();
    this.statusChangeMap = new HashMap<>();
    unackSohChanges.getSohStatusChanges().forEach(this::addSohStatusChange);
  }

  public String getStation() {
    return this.station;
  }

  /**
   * Check if an entry already exists in the map, if not add it.
   *
   * @param statusChange the {@link SohStatusChange}.
   */
  public boolean addSohStatusChange(SohStatusChange statusChange) {
    String key = statusChange.getChangedChannel() + "." + statusChange.getSohMonitorType();

    boolean added = false;

    // If not already in the map add it
    if (!this.statusChangeMap.containsKey(key)) {
      this.statusChangeMap.put(key, statusChange);
      added = true;
    }

    return added;
  }

  public void clearSohStatusChanges() {
    this.statusChangeMap.clear();
  }

  /**
   * Translate this back to the COI version to be sent on the Unack topic
   */
  public UnacknowledgedSohStatusChange getUnacknowledgedSohStatusChange() {
    return UnacknowledgedSohStatusChange.from(this.station, new HashSet<>(this.statusChangeMap.values()));
  }
}
