package gms.shared.stationdefinition.coi.utils.comparator;

import gms.shared.stationdefinition.coi.station.Station;

import java.util.Comparator;

public class StationComparator implements Comparator<Station> {

  @Override
  public int compare(Station c1, Station c2) {
    return Comparator.comparing(Station::getName)
      .thenComparing(s -> s.getEffectiveAt().orElse(null),
        Comparator.nullsLast(Comparator.naturalOrder()))
      .compare(c1, c2);
  }
}
