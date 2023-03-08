package gms.shared.event.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import gms.shared.stationdefinition.coi.channel.ChannelBandType;
import gms.shared.stationdefinition.coi.channel.ChannelDataType;
import gms.shared.stationdefinition.coi.channel.Location;

import java.util.Map;
import java.util.Optional;

@AutoValue
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ReceiverLocationsAndTypes {


  public abstract Optional<ChannelDataType> getReceiverDataType();

  public abstract Optional<ChannelBandType> getReceiverBandType();

  public abstract Map<String, Location> getReceiverLocationsByName();

  //TODO: Maybe use builder pattern here
  @JsonCreator
  public static ReceiverLocationsAndTypes from(
    @JsonProperty("receiverDataType") Optional<ChannelDataType> receiverDataType,
    @JsonProperty("receiverBandType") Optional<ChannelBandType> receiverBandType,
    @JsonProperty("receiverLocationsByName") Map<String, Location> receiverLocationsByName) {
    return new AutoValue_ReceiverLocationsAndTypes(receiverDataType, receiverBandType, receiverLocationsByName);
  }


}
