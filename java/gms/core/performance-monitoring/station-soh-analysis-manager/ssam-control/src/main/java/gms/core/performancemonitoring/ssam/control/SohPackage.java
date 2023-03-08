package gms.core.performancemonitoring.ssam.control;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableSet;
import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;
import gms.shared.frameworks.osd.coi.soh.StationSoh;

import java.util.Collection;
import java.util.Set;

/**
 * This class pairs a CapabilitySohRollup object with a set of correlated StationSoh objects. The
 * set can be added to later.
 */
@AutoValue
public abstract class SohPackage {

  public abstract Set<CapabilitySohRollup> getCapabilitySohRollups();

  public abstract Set<StationSoh> getStationSohs();

  public static SohPackage create(Collection<CapabilitySohRollup> capabilitySohRollup,
    Collection<StationSoh> correlatedStationSohs) {
    return new AutoValue_SohPackage(ImmutableSet.copyOf(capabilitySohRollup),
      ImmutableSet.copyOf(correlatedStationSohs));
  }

}
