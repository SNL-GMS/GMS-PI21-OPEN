package gms.dataacquisition.data.preloader.generator;

import gms.dataacquisition.data.preloader.GenerationSpec;
import gms.shared.frameworks.injector.RsdfIdModifier;
import gms.shared.frameworks.osd.api.OsdRepositoryInterface;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrame;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFrameMetadata;
import gms.shared.frameworks.osd.coi.waveforms.RawStationDataFramePayloadFormat;
import gms.shared.frameworks.osd.coi.waveforms.WaveformSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Generate raw station dataframes
 */
public class RsdfDataGenerator extends CoiDataGenerator<RawStationDataFrame, RsdfIdModifier> {

  private static final Logger logger = LoggerFactory.getLogger(RsdfDataGenerator.class);

  /**
   * Constructor
   *
   * @param generationSpec blueprint for generating data
   * @param sohRepository connection to OSD
   */
  public RsdfDataGenerator(GenerationSpec generationSpec,
    OsdRepositoryInterface sohRepository) {
    super(generationSpec, sohRepository);
  }

  @Override
  protected Collection<String> getSeedNames() {
    return stations()
      .map(Station::getName)
      .distinct()
      .collect(toList());
  }

  @Override
  protected RawStationDataFrame generateSeed(String stationName) {
    logger.debug("RawStationDataFrame seed generation - STARTING...");

    final var station = stations()
      .filter(s -> stationName.equals(s.getName()))
      .findFirst()
      .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationName));

    final var channelNames = station.channels()
      .map(Channel::getName)
      .distinct()
      .collect(toList());

    final Map<String, WaveformSummary> waveformSummaries =
      channelNames.stream().collect(toMap(c -> c, c -> WaveformSummary.from(c,
        seedTime, seedTime.plus(generationFrequency))));

    final RawStationDataFrame frame = RawStationDataFrame.builder()
      .generatedId()
      .setMetadata(RawStationDataFrameMetadata.builder()
        .setStationName(stationName)
        .setChannelNames(channelNames)
        .setPayloadFormat(RawStationDataFramePayloadFormat.CD11)
        .setPayloadStartTime(seedTime)
        .setPayloadEndTime(seedTime.plus(generationFrequency))
        .setReceptionTime(generationSpec.getReceptionTime())
        .setAuthenticationStatus(
          RawStationDataFrame.AuthenticationStatus.AUTHENTICATION_SUCCEEDED)
        .setWaveformSummaries(waveformSummaries)
        .build())
      .setRawPayload(new byte[50])
      .build();

    logger.debug("RawStationDataFrame seed generation - COMPLETE");
    return frame;
  }

  @Override
  protected RsdfIdModifier getModifier(Duration generationFrequency) {
    return new RsdfIdModifier(generationFrequency);
  }

  @Override
  protected void consumeRecords(Iterable<RawStationDataFrame> records) {
    logger.debug("RawStationDataFrame consuming records - STARTING...");
    sohRepository.storeRawStationDataFrames(convertToSet(records));
    logger.debug("RawStationDataFrame consuming records - COMPLETE");
  }


}
