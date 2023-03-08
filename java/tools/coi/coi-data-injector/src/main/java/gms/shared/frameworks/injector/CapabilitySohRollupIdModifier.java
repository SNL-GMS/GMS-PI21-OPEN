package gms.shared.frameworks.injector;

import gms.shared.frameworks.osd.coi.soh.CapabilitySohRollup;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CapabilitySohRollupIdModifier implements Modifier<Iterable<CapabilitySohRollup>> {

  private Instant time = null;

  private final Duration interval;

  public CapabilitySohRollupIdModifier() {
    this(Duration.ofSeconds(20));
  }

  public CapabilitySohRollupIdModifier(Duration interval) {
    this.interval = interval;
  }

  @Override
  public List<CapabilitySohRollup> apply(Iterable<CapabilitySohRollup> rollupList) {

    List<CapabilitySohRollup> newRollupList = new ArrayList<>();

    for (CapabilitySohRollup rollup : rollupList) {
      if (time == null) {
        time = rollup.getTime();
      }
      newRollupList.add(
        CapabilitySohRollup
          .create(UUID.randomUUID(),
            time,
            rollup.getGroupRollupSohStatus(),
            rollup.getForStationGroup(),
            rollup.getBasedOnStationSohs(),
            rollup.getRollupSohStatusByStation())
      );
      time = time.plus(interval);
    }
    return newRollupList;
  }

}
