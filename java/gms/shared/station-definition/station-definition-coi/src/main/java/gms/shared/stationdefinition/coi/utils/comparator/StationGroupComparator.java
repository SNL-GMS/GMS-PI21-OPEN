package gms.shared.stationdefinition.coi.utils.comparator;

import gms.shared.stationdefinition.coi.station.StationGroup;

import java.util.Comparator;

public class StationGroupComparator implements Comparator<StationGroup> {
  @Override
  public int compare(StationGroup c1, StationGroup c2) {
    return Comparator.comparing(StationGroup::getName)
      .thenComparing(s -> s.getEffectiveAt().orElse(null),
        Comparator.nullsLast(Comparator.naturalOrder()))
      .compare(c1, c2);
  }
}
