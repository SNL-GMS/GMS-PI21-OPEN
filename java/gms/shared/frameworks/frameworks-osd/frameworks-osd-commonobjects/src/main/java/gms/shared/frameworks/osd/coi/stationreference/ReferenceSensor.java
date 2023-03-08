package gms.shared.frameworks.osd.coi.stationreference;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.auto.value.AutoValue;
import com.google.auto.value.extension.memoized.Memoized;
import gms.shared.frameworks.osd.coi.provenance.InformationSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;


/**
 * A channel is an abstract entity specifying a basic type of measurement capability; the actual
 * hardware that produces the data stream for that channel is an instrument. The sensor class has a
 * manufacturer, a model, a serial number, and nominal calibration and response information (i.e.,
 * the specifications provided by the manufacturer), which are captured in the Calibration and
 * Response classes. While the type of information a channel records will not change, the actual
 * instrument used may (e.g., an upgrade to a more current model); hence Channel can point to more
 * than one Instrument, and Instrument includes on time and off time as attributes.
 */
@AutoValue
@JsonSerialize(as = ReferenceSensor.class)
@JsonDeserialize(builder = AutoValue_ReferenceSensor.Builder.class)
public abstract class ReferenceSensor {

  @JsonIgnore
  @Memoized
  public UUID getId() {
    return UUID.nameUUIDFromBytes(
      (getChannelName() + getInstrumentManufacturer() + getInstrumentModel() + getSerialNumber() + getNumberOfComponents()
        + getCornerPeriod() + getLowPassband() + getHighPassband() + getActualTime() + getSystemTime())
        .getBytes(StandardCharsets.UTF_16LE));
  }

  public abstract String getChannelName();

  public abstract String getInstrumentManufacturer();

  public abstract String getInstrumentModel();

  public abstract String getSerialNumber();

  public abstract int getNumberOfComponents();

  public abstract double getCornerPeriod();

  public abstract double getLowPassband();

  public abstract double getHighPassband();

  public abstract Instant getActualTime();

  public abstract Instant getSystemTime();

  public abstract InformationSource getInformationSource();

  public abstract String getComment();

  public abstract Builder toBuilder();

  public static Builder builder() {
    return new AutoValue_ReferenceSensor.Builder();
  }

  @AutoValue.Builder
  @JsonPOJOBuilder(withPrefix = "set")
  public abstract static class Builder {

    public abstract Builder setChannelName(String channelName);

    public abstract Builder setInstrumentManufacturer(String instrumentManufacturer);

    public abstract Builder setInstrumentModel(String instrumentModel);

    public abstract Builder setSerialNumber(String serialNumber);

    public abstract Builder setNumberOfComponents(int numberOfComponents);

    public abstract Builder setCornerPeriod(double cornerPeriod);

    public abstract Builder setLowPassband(double lowPassband);

    public abstract Builder setHighPassband(double highPassband);

    public abstract Builder setActualTime(Instant actualTime);

    public abstract Builder setSystemTime(Instant systemTime);

    public abstract Builder setInformationSource(InformationSource informationSource);

    public abstract Builder setComment(String comment);

    public abstract ReferenceSensor build();
  }
}

