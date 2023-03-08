package gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.file;

import gms.dataacquisition.stationreceiver.cd11.common.FrameUtilities;
import gms.dataacquisition.stationreceiver.cd11.common.enums.FrameType;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframe;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11ChannelSubframeHeader;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Data;
import gms.dataacquisition.stationreceiver.cd11.common.frames.Cd11Frame;
import gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils;
import gms.shared.frameworks.injector.Modifier;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.stream.Collectors;

import static gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils.getSequenceNumber;
import static gms.dataacquisition.stationreceiver.cd11.dataprovider.rsdfsource.ProviderUtils.readCd11Rsdf;
import static java.util.stream.Collectors.toList;

/**
 * Modifier used to iteratively loop over an input dataset of {@link RawStationDataFrame}s once
 * every provided interval indefinitely
 */
class RsdfFileSourceModifier implements Modifier<Iterable<RawStationDataFrame>> {

  private final Logger logger = LoggerFactory.getLogger(RsdfFileSourceModifier.class);

  private final Duration intervalDuration;
  private final long stationSequenceSpan;

  private Map<UUID, Pair<Long, RawStationDataFrameMetadata>> currentRsdfInfoById;

  public RsdfFileSourceModifier(Duration intervalDuration, long stationSequenceSpan) {
    this.intervalDuration = intervalDuration;
    this.stationSequenceSpan = stationSequenceSpan;
    this.currentRsdfInfoById = new HashMap<>();
  }

  @Override
  public List<RawStationDataFrame> apply(Iterable<RawStationDataFrame> rsdfIterable) {
    logger.debug("Applying shift to RSDFs");

    if (currentRsdfInfoById.isEmpty()) {
      rsdfIterable.forEach(rsdf -> currentRsdfInfoById.put(rsdf.getId(),
        Pair.of(getSequenceNumber(readCd11Rsdf(rsdf)), rsdf.getMetadata())));
    }

    List<RawStationDataFrame> newRsdfList = new ArrayList<>();

    for (RawStationDataFrame rsdf : rsdfIterable) {
      newRsdfList.add(modifyRsdf(rsdf));
    }

    currentRsdfInfoById = currentRsdfInfoById.entrySet().stream()
      .collect(Collectors.toMap(Entry::getKey, entry -> Pair
        .of(updateSequenceNumber(entry.getValue().getLeft()),
          updateMetadata(entry.getValue().getRight()))));

    newRsdfList.sort(Comparator.comparing(rsdf -> rsdf.getMetadata().getReceptionTime()));

    return newRsdfList;
  }

  private RawStationDataFrame modifyRsdf(RawStationDataFrame rsdf) {

    RawStationDataFrameMetadata currentMetadata = currentRsdfInfoById.get(rsdf.getId()).getRight();
    return rsdf.toBuilder()
      .generatedId()
      .setMetadata(currentMetadata)
      .setRawPayload(modifyRawPayload(rsdf, currentMetadata.getPayloadStartTime()))
      .build();
  }

  byte[] modifyRawPayload(RawStationDataFrame rsdFrame, Instant payloadStartTime) {
    try {
      Cd11Frame dataFrame = readCd11Rsdf(rsdFrame);
      Cd11Data data = FrameUtilities.asPayloadType(dataFrame.getPayload(), FrameType.DATA);

      Cd11ChannelSubframeHeader modifiedHeader = ProviderUtils
        .cloneAndModifyChannelSubframeHeader(data.getChanSubframeHeader(), payloadStartTime);
      List<Cd11ChannelSubframe> modifiedSubframes = data.getChannelSubframes().stream()
        .map(subframe -> ProviderUtils.cloneAndModifyChannelSubframe(subframe, payloadStartTime))
        .collect(toList());
      Cd11Data modifiedData = Cd11Data.builder()
        .setChanSubframeHeader(modifiedHeader)
        .setChannelSubframes(modifiedSubframes)
        .build();

      var modifiedFrame = dataFrame.toBuilder()
        .setHeader(dataFrame.getHeader().toBuilder().setSequenceNumber(currentRsdfInfoById.get(rsdFrame.getId()).getLeft()).build())
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

  private Long updateSequenceNumber(Long currentSequenceNumber) {
    return currentSequenceNumber + stationSequenceSpan;
  }

  private RawStationDataFrameMetadata updateMetadata(RawStationDataFrameMetadata metadataToUpdate) {

    Instant newStartTime = metadataToUpdate.getPayloadStartTime().plus(intervalDuration);
    Instant newEndTime = metadataToUpdate.getPayloadEndTime().plus(intervalDuration);

    final var waveformSummaries = metadataToUpdate.getWaveformSummaries().entrySet()
      .stream().map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(),
        WaveformSummary
          .from(e.getValue().getChannelName(), newStartTime, newEndTime)))
      .collect(
        Collectors.toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));

    return metadataToUpdate.toBuilder()
      .setPayloadStartTime(newStartTime)
      .setPayloadEndTime(newEndTime)
      .setReceptionTime(metadataToUpdate.getReceptionTime().plus(intervalDuration))
      .setWaveformSummaries(waveformSummaries)
      .build();
  }
}
