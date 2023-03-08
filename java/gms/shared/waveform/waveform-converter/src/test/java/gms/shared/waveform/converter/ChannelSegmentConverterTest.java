package gms.shared.waveform.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.utilities.javautilities.objectmapper.ObjectMapperFactory;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.waveform.coi.Waveform;
import gms.shared.waveform.coi.util.WaveformUtility;
import gms.shared.waveform.testfixture.ChannelSegmentTestFixtures;
import gms.utilities.waveformreader.WaveformReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class ChannelSegmentConverterTest {

  private static final File MAIN_DIR = new File(ChannelSegmentConverterTest.class.
    getClassLoader().getResource("WaveformFiles").getFile());


  private static final String E1WAVEFORMS1 = "E1Waveforms1.json";
  private static final String E1WAVEFORMS2 = "E1Waveforms2.json";
  private static final String S4WAVEFORMS = "S4Waveforms.json";
  private static final String I4_AND_F4_WAVEFORMS = "I4andF4Waveforms.json";
  private static final String T4WAVEFORMS = "T4Waveforms.json";
  private static final String S2WAVEFORMS = "S2Waveforms.json";
  private static final String S3WAVEFORMS = "S3Waveforms.json";
  private static final String BAD_DATA = "BAD_DATA.w";

  private ChannelSegmentConverter channelSegmentConverter;

  @BeforeEach
  void setUp() {
    channelSegmentConverter = ChannelSegmentConvertImpl.create();
  }

  @Test
  void testConvertChannelSegmentSingleE1Waveform() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForSingleE1();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelE1();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();
    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(channel, startTime, endTime, startTime);
    ChannelSegment<Waveform> convertedChannelSegmentCsd =
      channelSegmentConverter.convert(channelSegmentDescriptor, wfdiscDaos);
    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = null;
    WfdiscDao wfdiscDao = wfdiscDaos.get(0);
    try (InputStream in = new FileInputStream(new File(wfdiscDao.getDir(), wfdiscDao.getDfile()))) {
      double[] values = WaveformReader.readSamples(in, wfdiscDao.getDataType().toString(), wfdiscDao.getNsamp(), wfdiscDao.getFoff(), 0);
      double calibration = wfdiscDao.getCalib();
      for (int i = 0; i < values.length; i++) {
        values[i] *= calibration;
      }

      waveforms = List.of(Waveform.create(wfdiscDao.getTime(), wfdiscDao.getSampRate(), values));
    } catch (IOException e) {
      fail("Unable to read waveforms json file", e);
    }

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());
    ChannelSegment<Waveform> channelSegmentCorrectCsd =
      ChannelSegmentTestFixtures.createChannelSegmentChannelSegmentDescriptor(channelSegmentDescriptor, waveforms);

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
    assertEquals(channelSegmentCorrectCsd, convertedChannelSegmentCsd);
  }

  @Test
  void testConvertChannelSegmentMultipleE1Waveforms() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForMultipleE1();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelE1();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(channel, startTime, endTime, startTime);
    ChannelSegment<Waveform> convertedChannelSegmentCsd =
      channelSegmentConverter.convert(channelSegmentDescriptor, wfdiscDaos);
    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = wfdiscDaos.stream()
      .map(wfdiscDao -> {
        try (InputStream in = new FileInputStream(new File(wfdiscDao.getDir(), wfdiscDao.getDfile()))) {
          double[] values = WaveformReader.readSamples(in, wfdiscDao.getDataType().toString(), wfdiscDao.getNsamp(), wfdiscDao.getFoff(), 0);
          double calibration = wfdiscDao.getCalib();
          for (int i = 0; i < values.length; i++) {
            values[i] *= calibration;
          }

          return Waveform.create(wfdiscDao.getTime(), wfdiscDao.getSampRate(), values);
        } catch (IOException ex) {
          fail("error loading waveforms");
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());
    ChannelSegment<Waveform> channelSegmentCorrectCsd =
      ChannelSegmentTestFixtures.createChannelSegmentChannelSegmentDescriptor(channelSegmentDescriptor, waveforms);

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
    assertEquals(channelSegmentCorrectCsd, convertedChannelSegmentCsd);
  }

  @Test
  void testConvertChannelSegmentS4FormatWaveform() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForS4();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelS4();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);

    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = null;
    WfdiscDao wfdiscDao = wfdiscDaos.get(0);
    try (InputStream in = new FileInputStream(new File(wfdiscDao.getDir(), wfdiscDao.getDfile()))) {
      double readingDurationNano = Duration.between(startTime, endTime).toNanos();

      int samplesToRead = (int) ((readingDurationNano / 1_000_000_000L) * wfdiscDao.getSampRate() + 1);
      double[] values = WaveformReader.readSamples(in, wfdiscDao.getDataType().toString(), samplesToRead, wfdiscDao.getFoff(), 0);
      double calibration = wfdiscDao.getCalib();
      for (int i = 0; i < values.length; i++) {
        values[i] *= calibration;
      }

      waveforms = List.of(Waveform.create(wfdiscDao.getTime(), wfdiscDao.getSampRate(), values));
    } catch (IOException e) {
      fail("Unable to read waveforms json file", e);
    }

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
  }

  @Test
  void testConvertChannelSegmentI4FormatWaveform() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForI4();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelI4F4S2();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = null;
    try {
      waveforms = getWaveforms(I4_AND_F4_WAVEFORMS);
    } catch (IOException e) {
      fail("Unable to read waveforms json file", e);
    }

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
  }

  @Test
  void testConvertChannelSegmentT4FormatWaveform() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForT4();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelT4();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = null;
    try {
      waveforms = getWaveforms(T4WAVEFORMS);
    } catch (IOException e) {
      fail("Unable to read waveforms json file", e);
    }

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
  }

  @Test
  void testConvertChannelSegmentS3FormatWaveform() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForS3();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelS3();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = null;
    try {
      waveforms = getWaveforms(S3WAVEFORMS);
    } catch (IOException e) {
      fail("Unable to read waveforms json file", e);
    }

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
  }

  @Test
  void testConvertChannelSegmentS2FormatWaveform() {
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForS2();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelI4F4S2();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = null;
    try {
      waveforms = getWaveforms(S2WAVEFORMS);
    } catch (IOException e) {
      fail("Unable to read waveforms json file", e);
    }

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
  }


  private static Stream<Arguments> testCombiningWaveformChannelSegments() {
    return Stream.of(
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForCombiningE1(), 1),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForCombiningMultipleE1(2), 1),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForCombiningMultipleE1(4), 1),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForCombiningMultipleE1(6), 1),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForNotCombiningE1(), 2),
      Arguments.of(ChannelSegmentTestFixtures.getWfdiscListForPartialCombining(), 2),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListWithBigDiffSampRate(), 2),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListWithSmallDiffSampRate(), 1),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForEnclosedE1(), 1),
      Arguments.of(ChannelSegmentTestFixtures.getTestWfdiscListForMultipleEnclosedE1(), 2)
    );
  }


  @ParameterizedTest
  @MethodSource("testCombiningWaveformChannelSegments")
  void checkForCombineWaveforms(List<WfdiscDao> wfdiscDaos, int numWaveforms) {
    // the sample waveforms should be combined, because they are "adjacent" within 1.5 samples
    Channel channel = ChannelSegmentTestFixtures.getTestChannelE1();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();
    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(channel, startTime, endTime, startTime);
    ChannelSegment<Waveform> convertedChannelSegmentCsd =
      channelSegmentConverter.convert(channelSegmentDescriptor, wfdiscDaos);
    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = wfdiscDaos.stream()
      .map(wfdiscDao -> {
        try (InputStream in = new FileInputStream(new File(wfdiscDao.getDir(), wfdiscDao.getDfile()))) {
          double[] values = WaveformReader.readSamples(in, wfdiscDao.getDataType().toString(), wfdiscDao.getNsamp(),
            wfdiscDao.getFoff(), 0);
          double calibration = wfdiscDao.getCalib();
          for (int i = 0; i < values.length; i++) {
            values[i] *= calibration;
          }
          return Waveform.create(wfdiscDao.getTime(), wfdiscDao.getSampRate(), values);
        } catch (IOException ex) {
          fail("error loading waveforms");
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
    waveforms = WaveformUtility.mergeWaveforms(waveforms, 1, 1.5);
    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());
    //Waveforms should have been merged...
    assertEquals(numWaveforms, channelSegmentCorrect.getTimeseries().size());
    assertEquals(numWaveforms, convertedChannelSegment.getTimeseries().size());

    ChannelSegment<Waveform> channelSegmentCorrectCsd =
      ChannelSegmentTestFixtures.createChannelSegmentChannelSegmentDescriptor(channelSegmentDescriptor, waveforms);

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
    assertEquals(channelSegmentCorrectCsd, convertedChannelSegmentCsd);
  }

  @Test
  void testBiggerGapWaveforms() {
    // the sample waveforms should NOT be combined, because the gap is too big! This basically just is a repeat of
    //one of the other tests, but just demonstrates explicitly that there is NO merging of the Wfdiscs.
    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForMultipleE1();
    Channel channel = ChannelSegmentTestFixtures.getTestChannelE1();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(channel, startTime, endTime, startTime);
    ChannelSegment<Waveform> convertedChannelSegmentCsd =
      channelSegmentConverter.convert(channelSegmentDescriptor, wfdiscDaos);
    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel, wfdiscDaos, startTime, endTime);

    List<Waveform> waveforms = wfdiscDaos.stream()
      .map(wfdiscDao -> {
        try (InputStream in = new FileInputStream(new File(wfdiscDao.getDir(), wfdiscDao.getDfile()))) {
          double[] values = WaveformReader.readSamples(in, wfdiscDao.getDataType().toString(), wfdiscDao.getNsamp(), wfdiscDao.getFoff(), 0);
          double calibration = wfdiscDao.getCalib();
          for (int i = 0; i < values.length; i++) {
            values[i] *= calibration;
          }

          return Waveform.create(wfdiscDao.getTime(), wfdiscDao.getSampRate(), values);
        } catch (IOException ex) {
          fail("error loading waveforms");
          return null;
        }
      })
      .filter(Objects::nonNull)
      .collect(Collectors.toList());

    ChannelSegment<Waveform> channelSegmentCorrect =
      ChannelSegmentTestFixtures.createChannelSegment(channel, waveforms,
        convertedChannelSegment.getId().getCreationTime());
    ChannelSegment<Waveform> channelSegmentCorrectCsd =
      ChannelSegmentTestFixtures.createChannelSegmentChannelSegmentDescriptor(channelSegmentDescriptor, waveforms);

    assertEquals(2, channelSegmentCorrect.getTimeseries().size());
    assertEquals(2, convertedChannelSegment.getTimeseries().size());

    assertEquals(channelSegmentCorrect, convertedChannelSegment);
    assertEquals(channelSegmentCorrectCsd, convertedChannelSegmentCsd);
  }

  @Test
  void testEmptyWfdisc() {
    Instant currentTime = Instant.now();
    List<WfdiscDao> wfdiscDaos = Collections.emptyList();
    Channel channel1 = ChannelSegmentTestFixtures.getTestChannelE1();

    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(channel1, Instant.EPOCH, currentTime, currentTime);
    ChannelSegment<Waveform> convertedChannelSegmentCsd =
      channelSegmentConverter.convert(channelSegmentDescriptor, wfdiscDaos);
    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel1, wfdiscDaos, Instant.EPOCH, currentTime);

    assertNull(convertedChannelSegment);
    assertNull(convertedChannelSegmentCsd);
  }

  @Test
  void testInputStreamError() {

    List<WfdiscDao> wfdiscDaos = ChannelSegmentTestFixtures.getTestWfdiscListForSingleE1();
    Channel channel1 = ChannelSegmentTestFixtures.getTestChannelE1();
    wfdiscDaos = setWfdiscDaoDir(wfdiscDaos);
    wfdiscDaos.get(0).setDfile(BAD_DATA);

    Instant startTime = wfdiscDaos.stream()
      .map(WfdiscDao::getTime)
      .min(Instant::compareTo)
      .orElseThrow();
    Instant endTime = wfdiscDaos.stream()
      .map(WfdiscDao::getEndTime)
      .max(Instant::compareTo)
      .orElseThrow();

    ChannelSegmentDescriptor channelSegmentDescriptor = ChannelSegmentDescriptor
      .from(channel1, startTime, endTime, startTime);
    ChannelSegment<Waveform> convertedChannelSegmentCsd =
      channelSegmentConverter.convert(channelSegmentDescriptor, wfdiscDaos);
    ChannelSegment<Waveform> convertedChannelSegment =
      channelSegmentConverter.convert(channel1, wfdiscDaos, startTime, endTime);
    assertNull(convertedChannelSegment);
    assertNull(convertedChannelSegmentCsd);
  }

  private List<WfdiscDao> setWfdiscDaoDir(List<WfdiscDao> wfdiscDaos) {

    return wfdiscDaos.stream()
      .map(wfdiscDao -> {
        wfdiscDao.setDir(MAIN_DIR + "/data");
        return wfdiscDao;
      })
      .collect(Collectors.toList());
  }

  private List<Waveform> getWaveforms(String fileName) throws IOException {
    File file = new File(MAIN_DIR + File.separator + fileName);
    ObjectMapper objectMapper = ObjectMapperFactory.getJsonObjectMapper();

    return objectMapper.readValue(file, new TypeReference<>() {
    });
  }

}
