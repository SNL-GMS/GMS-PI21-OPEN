package gms.shared.waveform.converter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelTypes;
import gms.shared.stationdefinition.coi.channel.ChannelTypesParser;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import gms.shared.utilities.logging.TimingLogger;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.waveform.coi.Waveform;
import gms.shared.waveform.coi.util.TimeseriesUtility;
import gms.shared.waveform.coi.util.WaveformUtility;
import gms.utilities.waveformreader.WaveformReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ChannelSegmentConvertImpl implements ChannelSegmentConverter {

  /**
   * creates and validates a new {@link ChannelSegmentConvertImpl}
   *
   * @return a {@link ChannelSegmentConvertImpl}
   */
  public static ChannelSegmentConvertImpl create() {
    return new ChannelSegmentConvertImpl();
  }

  private static final Logger logger = LoggerFactory.getLogger(ChannelSegmentConvertImpl.class);
  private static final TimingLogger<Optional<Waveform>> timingLogger = TimingLogger.create(logger);

  private static final double NANO_SECOND_PER_SECOND = 1e9;

  private static final double SAMPLE_DIFF_ALLOWED = 1.5;

  /**
   * Converts a Channel a list of WfdiscDao and File pairs into a {@link ChannelSegment<Waveform>}
   *
   * @param channel The channel corresponding to the wfdiscDaos
   * @param wfdiscDaos a list of  WfdiscDaos
   * @return A ChannelSegment representing the provided channel, wfdisDaos, and .w files
   */
  public ChannelSegment<Waveform> convert(Channel channel, List<WfdiscDao> wfdiscDaos, Instant startTime,
    Instant endTime) {

    Objects.requireNonNull(channel);
    Objects.requireNonNull(wfdiscDaos);

    if (wfdiscDaos.isEmpty()) {
      logger.warn("List of wfdiscs and files is empty, returning null ChannelSegment");
      return null;
    }

    List<Waveform> waveforms = readWaveforms(wfdiscDaos, channel, startTime, endTime);

    if (waveforms.isEmpty()) {
      return null;
    }

    var units = getUnits(channel);
    Collections.sort(waveforms);

    final Range<Instant> timeRange = TimeseriesUtility.computeSpan(waveforms);

    return ChannelSegment.from(channel, units, waveforms, timeRange.lowerEndpoint());
  }

  /**
   * Converts a Channel a list of WfdiscDao and File pairs into a {@link ChannelSegment<Waveform>}
   *
   * @param channelSegmentDescriptor The channel segment descriptor corresponding to the channel segment
   * @param wfdiscDaos a list of  WfdiscDaos
   * @return A ChannelSegment representing the provided channelSegmentDescriptor, wfdisDaos, and .w files
   */
  public ChannelSegment<Waveform> convert(ChannelSegmentDescriptor channelSegmentDescriptor,
    List<WfdiscDao> wfdiscDaos) {

    Objects.requireNonNull(channelSegmentDescriptor);
    Objects.requireNonNull(wfdiscDaos);

    if (wfdiscDaos.isEmpty()) {
      logger.warn("List of wfdiscs and files is empty, returning null ChannelSegment");
      return null;
    }

    var channel = channelSegmentDescriptor.getChannel();
    var startTime = channelSegmentDescriptor.getStartTime();
    var endTime = channelSegmentDescriptor.getEndTime();

    List<Waveform> waveformList = readWaveforms(wfdiscDaos, channel, startTime, endTime);

    if (waveformList.isEmpty()) {
      return null;
    }
    var units = getUnits(channel);
    Collections.sort(waveformList);

    return ChannelSegment.from(channelSegmentDescriptor, units, waveformList);
  }

  private List<Waveform> readWaveforms(List<WfdiscDao> wfdiscDaos, Channel channel, Instant startTime,
    Instant endTime) {
    return WaveformUtility.mergeWaveforms(wfdiscDaos.stream()
      .map(wfdiscDao -> timingLogger.apply("readWaveform",
        () -> tryReadWaveform(wfdiscDao, channel, startTime, endTime)))
      .flatMap(Optional::stream)
      .collect(Collectors.toList()), 1, SAMPLE_DIFF_ALLOWED);
  }


  private Optional<Waveform> tryReadWaveform(WfdiscDao wfdiscDao, Channel channel, Instant startTime,
    Instant endTime) {
    try {
      return Optional.of(readWaveform(wfdiscDao, channel, startTime, endTime));
    } catch (IOException e) {
      logger.warn("Error Reading Waveform for Wfdisc {}", wfdiscDao.getId(), e);
      return Optional.empty();
    }
  }

  /**
   * readWaveforms from a file, producing multiple waveforms if the file contains more data than a single double array
   * can hold
   *
   * @return single Waveform object
   */
  private Waveform readWaveform(WfdiscDao wfdiscDao, Channel channel, Instant startTime,
    Instant endTime) throws IOException {

    Instant wfdiscStartTime = wfdiscDao.getTime();
    Instant wfdiscEndTime = wfdiscDao.getEndTime();
    double sampRateSeconds = wfdiscDao.getSampRate();
    var dataType = wfdiscDao.getDataType();
    int nsamp = wfdiscDao.getNsamp();
    long foff = wfdiscDao.getFoff();


    Instant readingStartTime = startTime.isBefore(wfdiscStartTime) ? wfdiscStartTime : startTime;
    Instant readingEndTime = endTime.isAfter(wfdiscEndTime) ? wfdiscEndTime : endTime;
    long readingDurationNano = Duration.between(readingStartTime, readingEndTime).toNanos();
    long expectedSamplesToRead = ((long) (readingDurationNano / NANO_SECOND_PER_SECOND * sampRateSeconds)) + 1;
    long actualSamplesToRead = Math.min(nsamp, expectedSamplesToRead);

    long skipNanos = !startTime.isAfter(wfdiscStartTime) ? 0 : Duration.between(wfdiscStartTime, startTime).toNanos();
    long currentSkip = (long) (skipNanos / NANO_SECOND_PER_SECOND * sampRateSeconds);

    var file = new File(wfdiscDao.getDir() + File.separator + wfdiscDao.getDfile());
    try (InputStream inputStream = new FileInputStream(file)) {
      logger.info("Reading waveform for {}", channel.getName());
      logger.info("Start: {}, End: {}, skip: {}, num samples: {}", wfdiscStartTime, wfdiscEndTime, currentSkip, nsamp);
      double[] data = WaveformReader.readSamples(inputStream, dataType.toString(), (int) actualSamplesToRead, foff,
        (int) currentSkip);
      double calibration = wfdiscDao.getCalib();
      for (var i = 0; i < data.length; i++) {
        data[i] *= calibration;
      }

      return Waveform.create(readingStartTime, sampRateSeconds, data);
    }
  }

  private Units getUnits(Channel channel) {
    var siteChanKey = StationDefinitionIdUtility.getCssKey(channel);
    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(siteChanKey.getChannelCode());

    Preconditions.checkState(channelTypesOptional.isPresent(),
      "Could not parse channel types for given channel");
    var channelTypes = channelTypesOptional.get();

    return Units.determineUnits(channelTypes.getDataType());
  }
}
