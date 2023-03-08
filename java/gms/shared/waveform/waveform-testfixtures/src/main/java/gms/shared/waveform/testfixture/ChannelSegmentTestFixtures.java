package gms.shared.waveform.testfixture;


import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelTypes;
import gms.shared.stationdefinition.coi.channel.ChannelTypesParser;
import gms.shared.stationdefinition.coi.utils.Units;
import gms.shared.stationdefinition.dao.css.WfdiscDao;
import gms.shared.stationdefinition.dao.css.enums.DataType;
import gms.shared.stationdefinition.dao.css.enums.SegType;
import gms.shared.stationdefinition.repository.util.StationDefinitionIdUtility;
import gms.shared.waveform.coi.ChannelSegment;
import gms.shared.waveform.coi.ChannelSegmentDescriptor;
import gms.shared.waveform.coi.Waveform;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChannelSegmentTestFixtures {

  private ChannelSegmentTestFixtures() {
  }

  private static final Instant LOAD_DATE = Instant.parse("2010-09-24T00:00:00.000Z");
  private static final double CALB_PER = 1;
  private static final double E1_S4_SAMP_RATE = 40.0;
  private static final double E1_CALIB = 0.027633;
  private static final double S4_CALIB = 0.0633;
  private static final double I4_F4_S2_SAMP_RATE = 100.0;
  private static final double I4_F4_T4_S2_CALIB = 1;
  private static final double T4_SAMP_RATE = 5.0;
  private static final double S3_SAMP_RATE = 20.0;
  private static final double S3_CALIB = 1.0;
  private static final String E1_CHANNEL = "TEST.TEST1.BHZ";
  private static final String S4_CHANNEL = "TEST.TEST2.BHN";
  private static final String I4_F4_S2_CHANNEL = "TEST.TEST3.HHE";
  private static final String T4_CHANNEL = "TEST.TEST4.BHE";
  private static final String S3_CHANNEL = "TEST.TEST5.SHZ";
  private static final String FILE_FOR_MERGING1 = "Mergeable1.w";
  private static final String FILE_FOR_MERGING2 = "Mergeable2.w";
  private static final String FILE_FOR_MULTIPLE_E1 = "MultipleE1_1.w";
  private static final Instant I4_F4_S2_START_TIME = Instant.parse("2011-01-01T00:00:23.552Z");
  private static final Instant I4_F4_S2_END_TIME = Instant.parse("2011-01-01T02:57:20.384Z");
  private static final Instant[] LIST_OF_START_TIMES = {
    Instant.parse("2010-05-21T02:00:00.000Z"),
    Instant.parse("2010-05-21T02:48:00.000Z"),
    Instant.parse("2010-05-21T03:36:00.000Z"),
    Instant.parse("2010-05-21T04:26:00.000Z"),
    Instant.parse("2010-05-21T05:14:00.000Z")};
  private static final Instant[] LIST_OF_END_TIMES = {
    Instant.parse("2010-05-21T03:59:59.950Z"),
    Instant.parse("2010-05-21T04:59:59.950Z"),
    Instant.parse("2010-05-21T05:59:59.950Z"),
    Instant.parse("2010-05-21T06:59:59.950Z")};

  public static Channel getTestChannel(String name) {
    return Channel.builder()
      .setName(name)
      .setEffectiveAt(Instant.parse("2000-02-20T05:59:35.680Z"))
      .build();
  }

  public static Channel getTestChannelE1() {
    return getTestChannel(E1_CHANNEL);
  }

  public static Channel getTestChannelS4() {
    return getTestChannel(S4_CHANNEL);
  }

  public static Channel getTestChannelI4F4S2() {
    return getTestChannel(I4_F4_S2_CHANNEL);
  }

  public static Channel getTestChannelT4() {
    return getTestChannel(T4_CHANNEL);
  }

  public static Channel getTestChannelS3() {
    return getTestChannel(S3_CHANNEL);
  }

  public static List<WfdiscDao> getTestWfdiscListForMultipleE1() {

    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MULTIPLE_E1)
      .setRange(
        Range.openClosed(Instant.parse("2010-05-21T01:59:17.760Z"),
          Instant.parse("2010-05-21T03:59:26.720Z")))
      .setNumSamples(288000)
      .setFoff(465272L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, E1_S4_SAMP_RATE, E1_CALIB, DataType.E1, channel));

    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("MultipleE1_2.w")
      .setRange(
        Range.openClosed(Instant.parse("2010-05-21T03:59:26.720Z"),
          Instant.parse("2010-05-21T05:59:35.680Z")))
      .setNumSamples(288000)
      .setFoff(431056L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, E1_S4_SAMP_RATE, E1_CALIB, DataType.E1, channel));


    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListForCombiningE1() {

    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0],
          LIST_OF_END_TIMES[0]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING2)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[1],
          LIST_OF_END_TIMES[1]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListForCombiningMultipleE1(int numOfWfdiscs) {
    //start with two and add more, depending on numOfWfdiscs (which is the number specified PLUS TWO...
    List<WfdiscDao> wfdiscDaos = getTestWfdiscListForCombiningE1();
    var channel = getTestChannelE1();
    for (var i = 0; i < numOfWfdiscs; i++) {
      Instant currentStartTime = LIST_OF_START_TIMES[2].plus(Duration.ofMinutes(i * 48L));
      Instant currentEndTime = LIST_OF_END_TIMES[3].plus(Duration.ofHours(i * 1L));
      var wfdiscCreationVariables = WfdiscCreationVariables.builder()
        .setFileName(FILE_FOR_MERGING1)
        .setRange(
          Range.openClosed(currentStartTime, currentEndTime))
        .setNumSamples(288000)
        .setFoff(0L)
        .build();
      wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    }

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListForNotCombiningE1() {
    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0],
          LIST_OF_END_TIMES[0]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING2)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[1].plusSeconds(2L),
          LIST_OF_END_TIMES[1]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListForEnclosedE1() {
    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0],
          LIST_OF_END_TIMES[1]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("SingleE1.w")
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0].plusSeconds(60),
          LIST_OF_END_TIMES[0].minusSeconds(60)))
      .setNumSamples(280000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListForMultipleEnclosedE1() {
    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();
    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0],
          LIST_OF_END_TIMES[0]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MULTIPLE_E1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0].plusSeconds(60),
          LIST_OF_START_TIMES[0].plusSeconds(600)))
      .setNumSamples(44000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));

    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[3],
          LIST_OF_END_TIMES[2]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MULTIPLE_E1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[3].plusSeconds(60),
          LIST_OF_START_TIMES[3].plusSeconds(600)))
      .setNumSamples(44000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListWithBigDiffSampRate() {
    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0],
          LIST_OF_END_TIMES[0]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING2)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[1],
          LIST_OF_END_TIMES[1]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 40.00, 0.0079, DataType.E1, channel));

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getTestWfdiscListWithSmallDiffSampRate() {
    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING1)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[0],
          LIST_OF_END_TIMES[0]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, 0.0079, DataType.E1, channel));
    wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName(FILE_FOR_MERGING2)
      .setRange(
        Range.openClosed(LIST_OF_START_TIMES[1],
          LIST_OF_END_TIMES[1]))
      .setNumSamples(288000)
      .setFoff(0L)
      .build();
    wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 99.50, 0.0079, DataType.E1, channel));

    return wfdiscDaos;
  }

  public static List<WfdiscDao> getWfdiscListForPartialCombining() {
    List<WfdiscDao> wfdiscDaos = new ArrayList<>();
    var channel = getTestChannelE1();

    for (Instant startTime : LIST_OF_START_TIMES) {
      var wfdiscCreationVariables = WfdiscCreationVariables.builder()
        .setFileName(FILE_FOR_MERGING1)
        .setRange(
          Range.openClosed(startTime,
            startTime.plus(Duration.ofMinutes(60L))))
        .setNumSamples(288000)
        .setFoff(0L)
        .build();
      wfdiscDaos.add(createWfdisc(wfdiscCreationVariables, 100.00, .0079, DataType.E1, channel));
    }
    return wfdiscDaos;
  }


  public static List<WfdiscDao> getTestWfdiscListForSingleE1() {

    var channel = getTestChannelE1();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("SingleE1.w")
      .setRange(
        Range.openClosed(Instant.parse("1998-02-21T23:59:08.800Z"),
          Instant.parse("1998-02-22T01:59:17.760Z")))
      .setNumSamples(288000)
      .setFoff(458656L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, E1_S4_SAMP_RATE, E1_CALIB, DataType.E1, channel));
  }

  public static List<WfdiscDao> getTestWfdiscListForS4() {

    var channel = getTestChannelS4();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("S4.w")
      .setRange(
        Range.openClosed(Instant.parse("2010-05-21T13:38:20.800Z"),
          Instant.parse("2010-05-21T14:02:22.592Z")))
      .setNumSamples(59200)
      .setFoff(5612800L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, E1_S4_SAMP_RATE, S4_CALIB, DataType.S4, channel));
  }

  public static List<WfdiscDao> getTestWfdiscListForI4() {

    var channel = getTestChannelI4F4S2();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("I4.w")
      .setRange(
        Range.openClosed(I4_F4_S2_START_TIME, I4_F4_S2_END_TIME))
      .setNumSamples(1061096)
      .setFoff(0L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, I4_F4_S2_SAMP_RATE, I4_F4_T4_S2_CALIB, DataType.I4, channel));
  }

  public static List<WfdiscDao> getTestWfdiscListForF4() {

    var channel = getTestChannelI4F4S2();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("F4.w")
      .setRange(
        Range.openClosed(I4_F4_S2_START_TIME,
          I4_F4_S2_END_TIME))
      .setNumSamples(1061096)
      .setFoff(0L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, I4_F4_S2_SAMP_RATE, I4_F4_T4_S2_CALIB, DataType.F4, channel));
  }

  public static List<WfdiscDao> getTestWfdiscListForT4() {

    var channel = getTestChannelT4();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("T4.w")
      .setRange(
        Range.openClosed(Instant.parse("1994-01-17T12:45:38.688Z"),
          Instant.parse("1994-01-17T13:50:05.312Z")))
      .setNumSamples(19291)
      .setFoff(0L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, T4_SAMP_RATE, I4_F4_T4_S2_CALIB, DataType.T4, channel));
  }

  public static List<WfdiscDao> getTestWfdiscListForS3() {

    var channel = getTestChannelS3();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("S3.w")
      .setRange(
        Range.openClosed(Instant.parse("2004-12-25T23:59:53.856Z"),
          Instant.parse("2004-12-26T02:00:02.816Z")))
      .setNumSamples(144000)
      .setFoff(0L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, S3_SAMP_RATE, S3_CALIB, DataType.S3, channel));
  }

  public static List<WfdiscDao> getTestWfdiscListForS2() {

    var channel = getTestChannelI4F4S2();

    var wfdiscCreationVariables = WfdiscCreationVariables.builder()
      .setFileName("S2.w")
      .setRange(
        Range.openClosed(I4_F4_S2_START_TIME, I4_F4_S2_END_TIME))
      .setNumSamples(1061096)
      .setFoff(0L)
      .build();

    return List.of(createWfdisc(wfdiscCreationVariables, I4_F4_S2_SAMP_RATE, I4_F4_T4_S2_CALIB, DataType.S2, channel));
  }

  public static ChannelSegment<Waveform> createChannelSegmentChannelSegmentDescriptor(
    ChannelSegmentDescriptor channelSegmentDescriptor, List<Waveform> waveforms) {

    var siteChanKey = StationDefinitionIdUtility.getCssKey(channelSegmentDescriptor.getChannel());
    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(siteChanKey.getChannelCode());

    Preconditions.checkState(channelTypesOptional.isPresent(),
      "Could not parse channel types for given channel");
    var channelTypes = channelTypesOptional.get();
    var units = Units.determineUnits(channelTypes.getDataType());

    return ChannelSegment.from(channelSegmentDescriptor, units, waveforms);
  }

  public static ChannelSegment<Waveform> createChannelSegment(Channel channel,
    List<Waveform> waveforms, Instant creationTime) {

    var siteChanKey = StationDefinitionIdUtility.getCssKey(channel);
    Optional<ChannelTypes> channelTypesOptional = ChannelTypesParser
      .parseChannelTypes(siteChanKey.getChannelCode());

    Preconditions.checkState(channelTypesOptional.isPresent(),
      "Could not parse channel types for given channel");
    var channelTypes = channelTypesOptional.get();
    var units = Units.determineUnits(channelTypes.getDataType());

    return ChannelSegment.from(channel, units, waveforms, creationTime);
  }

  public static ChannelSegment<Waveform> createChannelSegment(Channel channel,
    List<Waveform> waveforms) {
    return createChannelSegment(channel, waveforms, Instant.now());
  }

  public static WfdiscDao createWfdisc(WfdiscCreationVariables wfdiscCreationVariables,
    double sampRate, double calib, DataType dataType, Channel channel) {

    var siteChanKey = StationDefinitionIdUtility.getCssKeyFromName(channel.getName());

    var wfdiscDao = new WfdiscDao();

    wfdiscDao.setId(123);
    wfdiscDao.setStationCode(siteChanKey.getStationCode());
    wfdiscDao.setChannelCode(siteChanKey.getChannelCode());
    wfdiscDao.setTime(wfdiscCreationVariables.getRange().lowerEndpoint());
    wfdiscDao.setEndTime(wfdiscCreationVariables.getRange().upperEndpoint());
    wfdiscDao.setNsamp(wfdiscCreationVariables.getNumSamples());
    wfdiscDao.setSampRate(sampRate);
    wfdiscDao.setCalib(calib);
    wfdiscDao.setCalper(CALB_PER);
    wfdiscDao.setInsType(wfdiscDao.getInsType());
    wfdiscDao.setSegType(SegType.ORIGINAL);
    wfdiscDao.setDataType(dataType);
    wfdiscDao.setDir("/data/");
    wfdiscDao.setDfile(wfdiscCreationVariables.getFileName());
    wfdiscDao.setFoff(wfdiscCreationVariables.getFoff());
    wfdiscDao.setLoadDate(LOAD_DATE);

    return wfdiscDao;
  }

}
