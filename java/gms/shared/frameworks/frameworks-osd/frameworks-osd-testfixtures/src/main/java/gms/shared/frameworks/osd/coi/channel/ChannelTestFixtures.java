package gms.shared.frameworks.osd.coi.channel;

import gms.shared.frameworks.osd.coi.util.RandomUtility;

public class ChannelTestFixtures {

  private ChannelTestFixtures() {
  }

  // Raw
  private static final Channel asarAs01Bhz = ChannelFactory
    .rawFromReferenceChannel(ReferenceChannelTestFixtures.asarAs01Bhz(), "ASAR", "AS01");

  private static final Channel pdarPd01Shz = ChannelFactory
    .rawFromReferenceChannel(ReferenceChannelTestFixtures.pdarPd01Shz(), "PDAR", "PD01");

  private static final Channel txarTx01Shz = ChannelFactory
    .rawFromReferenceChannel(ReferenceChannelTestFixtures.txarTx01Shz(), "TXAR", "TX01");

  public static Channel asarAs01Bhz() {
    return asarAs01Bhz;
  }

  public static Channel pdarPd01Shz() {
    return pdarPd01Shz;
  }

  public static Channel txarTx01Shz() {
    return txarTx01Shz;
  }

  public static Channel randomSingleStationChannel() {
    String stationName = RandomUtility.randomUpperCase(3);
    ReferenceChannel referenceChannel = ReferenceChannelTestFixtures.randomChannel();

    return ChannelFactory.rawFromReferenceChannel(referenceChannel, stationName, stationName);
  }

}
