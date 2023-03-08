package gms.shared.frameworks.injector;

import gms.shared.frameworks.osd.coi.soh.StationSoh;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StationSohIdModifier implements Modifier<Iterable<StationSoh>> {

  private Instant time = null;

  private final Duration interval;

  public StationSohIdModifier() {
    this(Duration.ofSeconds(20));
  }

  public StationSohIdModifier(Duration interval) {
    this.interval = interval;
  }

  @Override
  public List<StationSoh> apply(Iterable<StationSoh> stationSohList) {

    List<StationSoh> newStationSohList = new ArrayList<>();

    for (StationSoh stationSoh : stationSohList) {
      if (time == null) {
        time = stationSoh.getTime();
      }
      newStationSohList.add(
        StationSoh.create(time,
          stationSoh.getStationName(),
          stationSoh.getSohMonitorValueAndStatuses(),
          stationSoh.getSohStatusRollup(),
          stationSoh.getChannelSohs(),
          stationSoh.getAllStationAggregates()));

      time = time.plus(interval);
    }
    return newStationSohList;
  }
}