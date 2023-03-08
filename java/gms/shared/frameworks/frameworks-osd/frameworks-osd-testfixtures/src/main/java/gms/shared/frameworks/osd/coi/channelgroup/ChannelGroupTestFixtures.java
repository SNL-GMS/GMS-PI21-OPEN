package gms.shared.frameworks.osd.coi.channelgroup;

import gms.shared.frameworks.osd.coi.channel.ChannelGroup;
import gms.shared.frameworks.osd.coi.channel.ChannelGroup.Type;
import gms.shared.frameworks.osd.coi.signaldetection.Location;

import java.util.List;

import static gms.shared.frameworks.osd.coi.channel.ChannelTestFixtures.asarAs01Bhz;
import static gms.shared.frameworks.osd.coi.channel.ChannelTestFixtures.pdarPd01Shz;
import static gms.shared.frameworks.osd.coi.channel.ChannelTestFixtures.txarTx01Shz;

public class ChannelGroupTestFixtures {

  private ChannelGroupTestFixtures() {
  }

  private static final ChannelGroup asarAs01 = ChannelGroup.from(
    "AS01",
    "Alice_Springs_Array,_Australia",
    Location.from(-23.6647, 133.9508, 0.0, 0.605),
    Type.SITE_GROUP,
    List.of(asarAs01Bhz()));

  private static final ChannelGroup pdarPd01 = ChannelGroup.from(
    "PD01",
    "Pinedale,_Wyoming:_USA_array_element",
    Location.from(42.76738, -109.5579, 0.0, 2.215),
    Type.SITE_GROUP,
    List.of(pdarPd01Shz()));

  private static final ChannelGroup txarPd01 = ChannelGroup.from(
    "TX01",
    "TXAR_Array,_Texas",
    Location.from(29.33426, -103.66768, 0.0, 0.991),
    Type.SITE_GROUP,
    List.of(txarTx01Shz()));

  public static ChannelGroup asarAs01() {
    return asarAs01;
  }

  public static ChannelGroup pdarPd01() {
    return pdarPd01;
  }

  public static ChannelGroup txarTx01() {
    return txarPd01;
  }

}
