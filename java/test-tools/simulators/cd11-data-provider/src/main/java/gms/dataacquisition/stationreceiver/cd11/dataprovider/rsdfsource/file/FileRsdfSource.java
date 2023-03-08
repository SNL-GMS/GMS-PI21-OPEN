package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframeHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.configuration.FileRsdfSourceConfig;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.RsdfSource;
import gms.shared.frameworks.injector.FluxFactory;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils.readCd11Rsdf;
import static java.util.stream.Collectors.toList;

/**
 * Implementation of the {@link RsdfSource} interface that provides data consumed and looped from a
 * specified directory containing a structured dataset of {@link RawStationDataFrame}s separated
 * into station-specific subdirectories
 * <p>
 * Looping behavior assumes that the start of the dataset across all stations will be repeated
 * immediately after the last payload end time of the dataset across all stations.
 */
public class FileRsdfSource implements RsdfSource {

  private static final Logger logger = LoggerFactory
    .getLogger(FileRsdfSource.class);

  private final ObjectMapper objectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
  private final String dataLocation;
  private final Instant referenceTime;
  private final Duration initialDelay;

  protected FileRsdfSource(FileRsdfSourceConfig config) {
    this.dataLocation = config.getDataLocation();
    this.referenceTime = config.getReferenceTime();
    this.initialDelay = Duration.ofSeconds(config.getInitialDelaySeconds().orElse(0L));
  }

  public static FileRsdfSource create(FileRsdfSourceConfig config) {
    return new FileRsdfSource(config);
  }

  @Override
  public Flux<RawStationDataFrame> getRsdfFlux() {
    logger.info("Searching {} for rsdf seed files...", dataLocation);
    final var seedDataDirectory = new File(dataLocation);
    if (!seedDataDirectory.exists() || !seedDataDirectory.isDirectory()) {
      throw new IllegalArgumentException(
        String.format("Directory of test files could not be found: %s", dataLocation));
    } else {
      final var rsdfSeedFiles = Stream.of(seedDataDirectory.listFiles())
        .filter(d -> d.exists() && d.isDirectory())
        .flatMap(d -> Stream.of(d.listFiles()))
        .filter(f -> f.isFile() && f.getName().toLowerCase(Locale.ENGLISH).endsWith(".json"))
        .collect(toList());

      logger.info("{} rsdf seed files found", rsdfSeedFiles.size());

      final var scheduler = Schedulers.single();
      Instant now = getNowFromScheduler(scheduler);

      var seedReferenceShift = Duration.between(referenceTime, now.plus(initialDelay));

      var rsdfsByStation = rsdfSeedFiles.stream()
        .map(file -> parseAndOffsetSeedData(file, seedReferenceShift))
        .map(stationRsdfs -> stationRsdfs
          .sorted(Comparator.comparing(r -> r.getMetadata().getReceptionTime()))
          .collect(toList()))
        .collect(toList());

      Duration dataInterval = calculateInterval(rsdfsByStation.stream()
        .flatMap(List::stream)
        .collect(toList()), now);

      return Flux.fromIterable(rsdfsByStation)
        .map(stationRsdfSeed -> {
          var infFluxScheduler = Schedulers.newSingle("delayScheduler");
          return FluxFactory.createOrderedInfiniteFlux(
              initialDelay,
              dataInterval,
              () -> stationRsdfSeed,
              new RsdfFileSourceModifier(dataInterval, getSequenceNumberSpan(stationRsdfSeed)),
              infFluxScheduler)
            .flatMapSequential(seed -> delayRsdfUntilReceptionTime(seed, infFluxScheduler));
        })
        .flatMap(Flux::merge, rsdfsByStation.size());
    }
  }

  private Duration calculateInterval(List<RawStationDataFrame> stationRsdfSeeds,
    Instant seedReferenceTime) {
    return Duration.between(
      seedReferenceTime,
      stationRsdfSeeds.stream()
        .map(RawStationDataFrame::getMetadata)
        .map(RawStationDataFrameMetadata::getReceptionTime)
        .max(Instant::compareTo).orElseThrow()
    );
  }

  private Mono<RawStationDataFrame> delayRsdfUntilReceptionTime(RawStationDataFrame rsdf,
    Scheduler scheduler) {
    final Instant receptionTime = rsdf.getMetadata().getReceptionTime();
    final var delay = Duration.between(getNowFromScheduler(scheduler), receptionTime);
    logger.debug("Station Frame {} [{}:{}:{}] delayed to {}", rsdf.getMetadata().getStationName(),
      rsdf.getMetadata().getPayloadStartTime(),
      rsdf.getMetadata().getPayloadEndTime(),
      rsdf.getMetadata().getReceptionTime(), delay);
    return Mono.just(rsdf).delayElement(limitOffset(delay), scheduler);
  }

  private Instant getNowFromScheduler(reactor.core.scheduler.Scheduler scheduler) {
    return Instant.ofEpochMilli(scheduler.now(TimeUnit.MILLISECONDS));
  }

  private Stream<RawStationDataFrame> parseAndOffsetSeedData(File file, Duration dataSetShift) {
    try {
      final List<RawStationDataFrame> rawStationDataFrames = objectMapper
        .readValue(file, objectMapper.getTypeFactory()
          .constructCollectionType(List.class, RawStationDataFrame.class));
      return offsetRsdfSeedStationData(rawStationDataFrames, dataSetShift).stream();
    } catch (IOException e) {
      throw Exceptions.propagate(e);
    }
  }

  private List<RawStationDataFrame> offsetRsdfSeedStationData(
    List<RawStationDataFrame> rsdfsFromFile,
    Duration dataSetShift) {

    return rsdfsFromFile.stream()
      .map(rsdf -> offsetRsdfSeedData(rsdf, dataSetShift))
      .collect(toList());
  }

  private RawStationDataFrame offsetRsdfSeedData(RawStationDataFrame rawStationDataFrame,
    Duration dataSetShift) {

    final var metadata = rawStationDataFrame.getMetadata();

    Instant newPayloadStart = metadata.getPayloadStartTime().plus(dataSetShift);
    Instant newPayloadEnd = metadata.getPayloadEndTime().plus(dataSetShift);

    final var waveformSummaries = metadata.getWaveformSummaries().entrySet()
      .stream().map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(),
        WaveformSummary
          .from(e.getValue().getChannelName(), newPayloadStart, newPayloadEnd)))
      .collect(
        Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));

    return rawStationDataFrame.toBuilder()
      .setMetadata(
        metadata.toBuilder()
          .setPayloadStartTime(newPayloadStart)
          .setPayloadEndTime(newPayloadEnd)
          .setReceptionTime(metadata.getReceptionTime().plus(dataSetShift))
          .setWaveformSummaries(waveformSummaries)
          .build())
      .setRawPayload(modifyRawPayload(rawStationDataFrame, newPayloadStart))
      .build();
  }

  byte[] modifyRawPayload(RawStationDataFrame rsdFrame, Instant newPayloadStart) {
    try {
      Cd11Frame dataFrame = readCd11Rsdf(rsdFrame);
      Cd11Data data = FrameUtilities.asPayloadType(dataFrame.getPayload(), FrameType.DATA);

      Cd11ChannelSubframeHeader modifiedHeader = ProviderUtils
        .cloneAndModifyChannelSubframeHeader(data.getChanSubframeHeader(), newPayloadStart);
      List<Cd11ChannelSubframe> modifiedSubframes = data.getChannelSubframes().stream()
        .map(subframe -> ProviderUtils.cloneAndModifyChannelSubframe(subframe, newPayloadStart))
        .collect(toList());
      Cd11Data modifiedData = Cd11Data.builder()
        .setChanSubframeHeader(modifiedHeader)
        .setChannelSubframes(modifiedSubframes)
        .build();
      Cd11Frame modifiedFrame = dataFrame.toBuilder()
        .setPayload(modifiedData)
        .build();

      return modifiedFrame.toBytes();
    } catch (IllegalArgumentException e) {
      logger.error(
        "Error reading raw payload from source RSDF for station {}. Returning unmodified payload.",
        rsdFrame.getMetadata().getStationName(), e);
      return rsdFrame.getRawPayload().orElseThrow(() -> new IllegalStateException("rsdf did not contain RawPayload"));
    }
  }

  private long getSequenceNumberSpan(List<RawStationDataFrame> rsdfs) {
    final var seedSequenceNumbers = rsdfs.stream()
      .map(ProviderUtils::getSequenceNumber)
      .sorted(Comparator.comparing(Long::longValue).reversed())
      .collect(toList());

    return seedSequenceNumbers.get(0) - seedSequenceNumbers.get(seedSequenceNumbers.size() - 1) + 1;
  }

  private Duration limitOffset(Duration payloadDuration) {
    return payloadDuration.isNegative() ? Duration.ZERO : payloadDuration;
  }

}