package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.dao.css.BeamDao;
import gms.shared.stationdefinition.dao.css.WfdiscDao;

import java.time.Instant;
import java.util.Collection;


public class WaveformSeedData {

  private ImmutableList<WfdiscDao> wfdiscDaoImmutableList;

  private ImmutableList<BeamDao> beamDaoImmutableList;

  private Range<Instant> timeRange;

  private WaveformSeedData(Collection<WfdiscDao> wfdiscDaos, Collection<BeamDao> beamDaos, Instant seedDataStartTime,
    Instant seedDataEndTime) {
    beamDaoImmutableList = ImmutableList.copyOf(beamDaos);
    wfdiscDaoImmutableList = ImmutableList.copyOf(wfdiscDaos);
    timeRange = Range.closed(seedDataStartTime, seedDataEndTime);
  }

  public static WaveformSeedData create(Collection<WfdiscDao> wfdiscDaos, Collection<BeamDao> beamDaos,
    Instant seedDataStartTime,
    Instant seedDataEndTime) {

    return new WaveformSeedData(wfdiscDaos, beamDaos, seedDataStartTime, seedDataEndTime);
  }

  public ImmutableList<WfdiscDao> getWfdiscSeedData() {

    return wfdiscDaoImmutableList;
  }

  public ImmutableList<BeamDao> getBeamSeedData() {

    return beamDaoImmutableList;
  }

  public Range<Instant> getTimeRange() {
    return timeRange;
  }
}
