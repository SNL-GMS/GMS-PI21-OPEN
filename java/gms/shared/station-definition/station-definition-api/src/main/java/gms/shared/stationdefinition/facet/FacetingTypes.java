package gms.shared.stationdefinition.facet;

public enum FacetingTypes {
  STATION_GROUP_TYPE("StationGroup"),
  STATION_TYPE("Station"),
  CHANNEL_GROUP_TYPE("ChannelGroup"),
  CHANNEL_TYPE("Channel"),
  RESPONSE_TYPE("Response"),
  CHANNEL_SEGMENT_TYPE("ChannelSegment"),
  STATIONS_KEY("stations"),
  CHANNEL_GROUPS_KEY("channelGroups"),
  CHANNELS_KEY("channels"),
  RESPONSES_KEY("responses"),
  ID_CHANNEL_KEY("id.Channel"),
  ;

  private final String value;

  FacetingTypes(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return getValue();
  }
}
