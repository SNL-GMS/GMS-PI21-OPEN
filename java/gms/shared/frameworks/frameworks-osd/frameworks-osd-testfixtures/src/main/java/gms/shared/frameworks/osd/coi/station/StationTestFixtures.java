package gms.shared.frameworks.osd.coi.station;

import gms.shared.frameworks.osd.coi.channel.ChannelTestFixtures;
import gms.shared.frameworks.osd.coi.channelgroup.ChannelGroupTestFixtures;
import gms.shared.frameworks.osd.coi.signaldetection.Location;
import gms.shared.frameworks.osd.coi.signaldetection.Station;
import gms.shared.frameworks.osd.coi.signaldetection.StationGroup;
import gms.shared.frameworks.osd.coi.stationreference.RelativePosition;
import gms.shared.frameworks.osd.coi.stationreference.StationType;

import java.util.List;
import java.util.Map;

public class StationTestFixtures {

  private StationTestFixtures() {
  }

  private static final Station asar = Station.from(
    "ASAR",
    StationType.SEISMIC_ARRAY,
    "Alice_Springs_Array,_Australia",
    Map.of(ChannelTestFixtures.asarAs01Bhz().getName(), RelativePosition.from(0.11, 4.786, 0.0)),
    Location.from(-23.6664, 133.9044, 0.0, 0.607),
    List.of(ChannelGroupTestFixtures.asarAs01()),
    List.of(ChannelTestFixtures.asarAs01Bhz())
  );

  private static final Station pdar = Station.from(
    "PDAR",
    StationType.SEISMIC_ARRAY,
    "Pinedale,_Wyoming:_USA_array_element",
    Map.of(ChannelTestFixtures.pdarPd01Shz().getName(),
      RelativePosition.from(1.014, -2.066, 0.0)),
    Location.from(42.76738, -109.5579, 0.0, 2.215),
    List.of(ChannelGroupTestFixtures.pdarPd01()),
    List.of(ChannelTestFixtures.pdarPd01Shz())
  );

  private static final Station txar = Station.from(
    "TXAR",
    StationType.SEISMIC_ARRAY,
    "TXAR_Array,_Texas",
    Map.of(ChannelTestFixtures.txarTx01Shz().getName(),
      RelativePosition.from(-0.032, -0.001, 0.0)),
    Location.from(29.33426, -103.66768, 0.0, 0.991),
    List.of(ChannelGroupTestFixtures.txarTx01()),
    List.of(ChannelTestFixtures.txarTx01Shz())
  );

  public static StationGroup getStationGroup() {
    return StationGroup.from("Test Station Group",
      "Test StationGroup",
      List.of(asar, pdar, txar));
  }

  public static Station singleStation() {
    return asar;
  }

  public static Station asar() {
    return asar;
  }

  public static Station pdar() {
    return pdar;
  }

  public static Station txar() {
    return txar;
  }

}
