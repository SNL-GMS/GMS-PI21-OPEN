package gms.shared.stationdefinition.coi.utils;

import gms.shared.stationdefinition.coi.channel.Channel;
import gms.shared.stationdefinition.coi.channel.ChannelGroup;
import gms.shared.stationdefinition.coi.channel.FrequencyAmplitudePhase;
import gms.shared.stationdefinition.coi.channel.Response;
import gms.shared.stationdefinition.coi.station.Station;
import gms.shared.stationdefinition.coi.station.StationGroup;
import gms.shared.stationdefinition.coi.utils.comparator.ChannelComparator;
import gms.shared.stationdefinition.coi.utils.comparator.ChannelGroupComparator;
import gms.shared.stationdefinition.coi.utils.comparator.FrequencyAmplitudePhaseComparator;
import gms.shared.stationdefinition.coi.utils.comparator.ResponseComparator;
import gms.shared.stationdefinition.coi.utils.comparator.StationComparator;
import gms.shared.stationdefinition.coi.utils.comparator.StationGroupComparator;

import java.util.Comparator;

public class StationDefinitionCoiUtils {

  public static final Comparator<StationGroup> STATION_GROUP_COMPARATOR = new StationGroupComparator();
  public static final Comparator<Station> STATION_COMPARATOR = new StationComparator();
  public static final Comparator<ChannelGroup> CHANNEL_GROUP_COMPARATOR = new ChannelGroupComparator();
  public static final Comparator<Channel> CHANNEL_COMPARATOR = new ChannelComparator();
  public static final Comparator<Response> RESPONSE_COMPARATOR = new ResponseComparator();
  public static final Comparator<FrequencyAmplitudePhase> FREQUENCY_AMPLITUDE_PHASE_COMPARATOR = new FrequencyAmplitudePhaseComparator();

  private StationDefinitionCoiUtils() {
  }
}
