package gms.shared.stationdefinition.api;

import gms.shared.stationdefinition.api.channel.ChannelAccessorInterface;
import gms.shared.stationdefinition.api.channel.ChannelGroupAccessorInterface;
import gms.shared.stationdefinition.api.channel.ResponseAccessorInterface;
import gms.shared.stationdefinition.api.station.StationAccessorInterface;
import gms.shared.stationdefinition.api.station.StationGroupAccessorInterface;

import java.time.Instant;
import java.util.List;


public interface StationDefinitionAccessorInterface extends
  StationGroupAccessorInterface,
  StationAccessorInterface,
  ChannelGroupAccessorInterface,
  ChannelAccessorInterface,
  ResponseAccessorInterface {

  default void cache(List<String> stationGroupNames, Instant startTime, Instant endTime) {
    // no op
  }

}
