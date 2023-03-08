package gms.shared.stationdefinition.coi.channel;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import gms.shared.stationdefinition.coi.station.StationId;
import gms.shared.stationdefinition.coi.utils.Units;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Base class for {@link Channel} that also serves as the Channel's DTO.
 * The primary difference from Channel is this class has a getStationId operation
 * that is used in serialization but does not have a getStation operation (i.e. it only
 * has a reference to Station). The effect of this is a serialized Channel can only
 * ever have a reference to it's parent Station.
 * <p>
 * Could update Channel constructors to force the Station to be a reference; could
 * log warnings during serialization if the Station is not a reference, etc. to let
 * users know of this serialization limitation.
 * <p>
 * Ideally this class would be package-private or protected, but that causes problems for
 * the current ChannelComparator implementation.
 */
@JsonPropertyOrder(alphabetic = true)
abstract class ChannelBase {

  public abstract String getName();

  public abstract Optional<Instant> getEffectiveAt();

  @JsonUnwrapped
  @JsonProperty(access = Access.READ_ONLY)
  public abstract Optional<? extends ChannelBase.Data> getData();

  abstract static class Data {

    @JsonProperty("station")
    abstract StationId getStationId();

    public abstract String getCanonicalName();

    public abstract Optional<Instant> getEffectiveUntil();

    public abstract String getDescription();

    public abstract ChannelDataType getChannelDataType();

    public abstract ChannelBandType getChannelBandType();

    public abstract ChannelInstrumentType getChannelInstrumentType();

    public abstract ChannelOrientationType getChannelOrientationType();

    public abstract char getChannelOrientationCode();

    public abstract Units getUnits();

    public abstract double getNominalSampleRateHz();

    public abstract Location getLocation();

    public abstract Orientation getOrientationAngles();

    @JsonProperty("configuredInputs")
    abstract List<ChannelId> getChannelId();

    public abstract Map<String, Object> getProcessingDefinition();

    public abstract Map<ChannelProcessingMetadataType, Object> getProcessingMetadata();

    public abstract Optional<Response> getResponse();
  }
}
