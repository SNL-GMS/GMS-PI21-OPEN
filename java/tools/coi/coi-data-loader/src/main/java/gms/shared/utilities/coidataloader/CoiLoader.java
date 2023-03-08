package gms.shared.utilities.coidataloader;

import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.ChannelSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CoiLoader {

  private static Logger logger = LoggerFactory.getLogger(CoiLoader.class);

  private static final ExecutorService executor = Executors.newFixedThreadPool(
    Runtime.getRuntime().availableProcessors());

  private OsdRepositoryInterface osd;

  private CoiLoader(OsdRepositoryInterface osd) {
    this.osd = Objects.requireNonNull(osd, "null osd");
  }

  public static CoiLoader create(OsdRepositoryInterface osd) {
    return new CoiLoader(osd);
  }

  public void load(CoiDataSet data) {
    Objects.requireNonNull(data, "null data");

    // store station groups first and block on it; waveform data may depend on these station groups
    storeIfAny(data.getStationGroups(), osd::storeStationGroups, "station groups");

    // storing waveforms sequentially for now since threading broke in JPA
    // store waveforms first since that may indirectly stores channels
    // which might be referred to in e.g. FeatureMeasurements
//    data.getWaveforms().forEach(wf -> {
//      try {
//        osd.storeChannelSegments(List.of(wf));
//      } catch (Exception ex) {
//        logSegmentFailure(wf);
//        throw ex;
//      }
//    });
    // code to store waveforms in parallel; would replace sequential store of waveforms above.
    // polled separately to ensure it finishes before moving on (storing waveforms can store channels
    // that signal detections depend on).
    // pollFutures(data.getWaveforms()
    //     .map(wf -> executor.submit(() -> osd.storeChannelSegments(List.of(wf))))
    //     .collect(Collectors.toList()));

//    data.getFks().forEach(fk -> {
//      try {
//        osd.storeFkChannelSegments(List.of(fk));
//      } catch (Exception ex) {
//        logSegmentFailure(fk);
//        throw ex;
//      }
//    });
//
    final Collection<Future> futures = new ArrayList<>(storeStaRef(data.getStationReference()));
//    storeAsyncIfAny(data.getResponses(), osd::storeResponses, "processing responses")
//        .ifPresent(futures::add);
//    storeAsyncIfAny(data.getMasks(), osd::storeQcMasks, "QC masks").ifPresent(futures::add);
//    storeAsyncIfAny(data.getSignalDetections(), osd::storeSignalDetections, "signal detections")
//        .ifPresent(futures::add);

    logger.info("All async store calls submitted, polling futures to await results");
    pollFutures(futures);
  }

  private Collection<Future> storeStaRef(StationReference staRef) {
    return filterPresent(List.of(
      storeAsyncIfAny(staRef.getNetworks(), osd::storeReferenceNetwork, "reference networks"),
      storeAsyncIfAny(staRef.getStations(), osd::storeReferenceStation, "reference stations"),
      storeAsyncIfAny(staRef.getSites(), osd::storeReferenceSites, "reference sites"),
      storeAsyncIfAny(staRef.getChannels(), osd::storeReferenceChannels, "reference channels"),
      storeAsyncIfAny(staRef.getSensors(), osd::storeReferenceSensors, "reference sensors"),
      storeAsyncIfAny(staRef.getResponses(), osd::storeReferenceResponses, "reference responses"),
      storeAsyncIfAny(staRef.getNetworkMemberships(), osd::storeNetworkMemberships,
        "net memberships"),
      storeAsyncIfAny(staRef.getStationMemberships(), osd::storeStationMemberships,
        "sta memberships"),
      storeAsyncIfAny(staRef.getSiteMemberships(), osd::storeSiteMemberships,
        "site memberships")));
  }

  private static <T> void storeIfAny(Collection<T> coll,
    Consumer<Collection<T>> storeFunc, String name) {
    if (!coll.isEmpty()) {
      logger.info("Storing {} {} (synchronously)", coll.size(), name);
      storeFunc.accept(coll);
    }
  }

  private static <T> Optional<Future> storeAsyncIfAny(Collection<T> coll,
    Consumer<Collection<T>> storeFunc, String name) {
    if (coll.isEmpty()) {
      return Optional.empty();
    }
    logger.info("Requesting to store {} {} (asynchronously)", coll.size(), name);
    return Optional.of(executor.submit(() -> storeFunc.accept(coll)));
  }

  private static <T> Collection<T> filterPresent(Collection<Optional<T>> optionals) {
    return optionals.stream().flatMap(Optional::stream).collect(Collectors.toList());
  }

  private static void pollFutures(Collection<Future> futures) {
    int count = 0;
    for (Future f : futures) {
      logger.info("Polling future {} of {}", ++count, futures.size());
      try {
        f.get(30, TimeUnit.MINUTES);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static void logSegmentFailure(ChannelSegment cs) {
    logger.error("Storing segment failed for segment {} (time range [{}, {}]) for Channel {}",
      cs.getName(), cs.getStartTime(), cs.getEndTime(), cs.getChannel());
  }
}
