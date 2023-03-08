package gms.testtools.simulators.bridgeddatasourceanalysissimulator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import gms.shared.signaldetection.dao.css.AmplitudeDao;
import gms.shared.signaldetection.dao.css.ArrivalDao;
import gms.shared.stationdefinition.dao.css.WfTagDao;

import java.time.Instant;
import java.util.Collection;

public class ArrivalSeedData {

  private final ImmutableList<ArrivalDao> arrivals;
  private final ImmutableList<AmplitudeDao> amplitudeRecords;
  private final ImmutableList<WfTagDao> wfTags;
  private final Range<Instant> timeRange;

  private ArrivalSeedData(Collection<ArrivalDao> arrivalsDaos,
    Collection<AmplitudeDao> amplitudeDaos,
    Collection<WfTagDao> wfTagsDaos,
    Instant seedDataStartTime,
    Instant seedDataEndTime) {
    arrivals = ImmutableList.copyOf(arrivalsDaos);
    amplitudeRecords = ImmutableList.copyOf(amplitudeDaos);
    wfTags = ImmutableList.copyOf(wfTagsDaos);
    timeRange = Range.closed(seedDataStartTime, seedDataEndTime);
  }

  public static ArrivalSeedData create(Collection<ArrivalDao> arrivalDaos,
    Collection<AmplitudeDao> amplitudeDaos,
    Collection<WfTagDao> wfTagDaos,
    Instant seedDataStartTime,
    Instant seedDataEndTime) {

    return new ArrivalSeedData(arrivalDaos, amplitudeDaos, wfTagDaos, seedDataStartTime, seedDataEndTime);
  }

  public ImmutableList<ArrivalDao> getArrivals() {
    return arrivals;
  }

  public ImmutableList<AmplitudeDao> getAmplitudeRecords() {
    return amplitudeRecords;
  }

  public ImmutableList<WfTagDao> getWfTags() {
    return wfTags;
  }

  public Range<Instant> getTimeRange() {
    return timeRange;
  }
}
