package gms.shared.frameworks.osd.dao.channel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import gms.shared.frameworks.osd.coi.Units;
import gms.shared.frameworks.osd.coi.channel.Channel;
import gms.shared.frameworks.osd.coi.channel.ChannelBandType;
import gms.shared.frameworks.osd.coi.channel.ChannelDataType;
import gms.shared.frameworks.osd.coi.channel.ChannelInstrumentType;
import gms.shared.frameworks.osd.coi.channel.ChannelOrientationType;
import gms.shared.frameworks.osd.coi.datatransferobjects.CoiObjectMapperFactory;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;


@NamedQuery(name = "Channel.getChannelNames", query = "SELECT name FROM ChannelDao")
@Entity
@Table(name = "channel")
@TypeDef(
  name = "jsonb",
  typeClass = JsonBinaryType.class
)
public class ChannelDao implements Serializable {

  private static final long serialVersionUID = 1L;

  @Id
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "canonical_name", nullable = false)
  private String canonicalName;

  @Column(name = "description", nullable = false)
  private String description;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "channel_band_type")
  private ChannelBandType channelBandType;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "channel_instrument_type")
  private ChannelInstrumentType channelInstrumentType;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "channel_orientation_type")
  private ChannelOrientationType channelOrientationType;

  @Column(name = "channel_orientation_code")
  private char channelOrientationCode;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "channel_data_type")
  private ChannelDataType channelDataType;

  @Enumerated(value = EnumType.STRING)
  @Column(name = "units")
  private Units units;

  @Column(name = "nominal_sample_rate_hz")
  private double nominalSampleRateHz;

  @Embedded
  private LocationDao location;

  @Embedded
  private OrientationDao orientationAngles;

  @Type(type = "jsonb")
  @Column(name = "processingDefinition", columnDefinition = "jsonb")
  private String processingDefinition;

  @Type(type = "jsonb")
  @Column(name = "processingMetadata", columnDefinition = "jsonb")
  private String processingMetadata;

  private static class Builder {

    private String name;
    private String canonicalName;
    private String description;
    private ChannelBandType channelBandType;
    private ChannelInstrumentType channelInstrumentType;
    private ChannelOrientationType channelOrientationType;
    private char channelOrientationCode;
    private ChannelDataType channelDataType;
    private Units units;
    private double nominalSampleRateHz;
    private LocationDao location;
    private OrientationDao orientationAngles;
    private String processingDefinition;
    private String processingMetadata;

    public Builder withName(String name) {
      this.name = name;
      return this;
    }

    public Builder withCanonicalName(String canonicalName) {
      this.canonicalName = canonicalName;
      return this;
    }

    public Builder withDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder withChannelBandType(
      ChannelBandType channelBandType) {
      this.channelBandType = channelBandType;
      return this;
    }

    public Builder withChannelInstrumentType(
      ChannelInstrumentType channelInstrumentType) {
      this.channelInstrumentType = channelInstrumentType;
      return this;
    }

    public Builder withChannelOrientationType(
      ChannelOrientationType channelOrientationType) {
      this.channelOrientationType = channelOrientationType;
      return this;
    }

    public Builder withChannelOrientationCode(char channelOrientationCode) {
      this.channelOrientationCode = channelOrientationCode;
      return this;
    }

    public Builder withChannelDataType(
      ChannelDataType channelDataType) {
      this.channelDataType = channelDataType;
      return this;
    }

    public Builder withUnits(Units units) {
      this.units = units;
      return this;
    }

    public Builder withNominalSampleRateHz(double nominalSampleRateHz) {
      this.nominalSampleRateHz = nominalSampleRateHz;
      return this;
    }

    public Builder withLocation(
      LocationDao location) {
      this.location = location;
      return this;
    }

    public Builder withOrientationAngles(
      OrientationDao orientationAngles) {
      this.orientationAngles = orientationAngles;
      return this;
    }

    public Builder withProcessingDefinition(String processingDefinition) {
      this.processingDefinition = processingDefinition;
      return this;
    }

    public Builder withProcessingMetadata(String processingMetadata) {
      this.processingMetadata = processingMetadata;
      return this;
    }

    public ChannelDao build() {
      ChannelDao channelDao = new ChannelDao();
      channelDao.setName(name);
      channelDao.setCanonicalName(canonicalName);
      channelDao.setDescription(description);
      channelDao.setChannelBandType(channelBandType);
      channelDao.setChannelInstrumentType(channelInstrumentType);
      channelDao.setChannelOrientationType(channelOrientationType);
      channelDao.setChannelOrientationCode(channelOrientationCode);
      channelDao.setChannelDataType(channelDataType);
      channelDao.setUnits(units);
      channelDao.setNominalSampleRateHz(nominalSampleRateHz);
      channelDao.setLocation(location);
      channelDao.setOrientationAngles(orientationAngles);
      channelDao.setProcessingDefinition(processingDefinition);
      channelDao.setProcessingMetadata(processingMetadata);
      return channelDao;
    }
  }

  public ChannelDao() {
    // Empty constructor needed for JPA
  }

  public static ChannelDao from(Channel channel) {
    ObjectMapper jsonObjectMapper = CoiObjectMapperFactory.getJsonObjectMapper();
    String processingDefinition = jsonObjectMapper.valueToTree(channel.getProcessingDefinition())
      .toString();
    String processingMetadata = jsonObjectMapper.valueToTree(channel.getProcessingMetadata())
      .toString();

    return new Builder()
      .withName(channel.getName())
      .withCanonicalName(channel.getCanonicalName())
      .withDescription(channel.getDescription())
      .withChannelBandType(channel.getChannelBandType())
      .withChannelInstrumentType(channel.getChannelInstrumentType())
      .withChannelOrientationType(channel.getChannelOrientationType())
      .withChannelOrientationCode(channel.getChannelOrientationCode())
      .withChannelDataType(channel.getChannelDataType())
      .withUnits(channel.getUnits())
      .withNominalSampleRateHz(channel.getNominalSampleRateHz())
      .withLocation(new LocationDao(channel.getLocation()))
      .withOrientationAngles(new OrientationDao(channel.getOrientationAngles()))
      .withProcessingDefinition(processingDefinition)
      .withProcessingMetadata(processingMetadata)
      .build();
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCanonicalName() {
    return canonicalName;
  }

  public void setCanonicalName(String canonicalName) {
    this.canonicalName = canonicalName;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public ChannelBandType getChannelBandType() {
    return channelBandType;
  }

  public void setChannelBandType(
    ChannelBandType channelBandType) {
    this.channelBandType = channelBandType;
  }

  public ChannelInstrumentType getChannelInstrumentType() {
    return channelInstrumentType;
  }

  public void setChannelInstrumentType(
    ChannelInstrumentType channelInstrumentType) {
    this.channelInstrumentType = channelInstrumentType;
  }

  public ChannelOrientationType getChannelOrientationType() {
    return channelOrientationType;
  }

  public void setChannelOrientationType(
    ChannelOrientationType channelOrientationType) {
    this.channelOrientationType = channelOrientationType;
  }

  public char getChannelOrientationCode() {
    return channelOrientationCode;
  }

  public void setChannelOrientationCode(char channelOrientationCode) {
    this.channelOrientationCode = channelOrientationCode;
  }

  public ChannelDataType getChannelDataType() {
    return channelDataType;
  }

  public void setChannelDataType(
    ChannelDataType channelDataType) {
    this.channelDataType = channelDataType;
  }

  public Units getUnits() {
    return units;
  }

  public void setUnits(Units units) {
    this.units = units;
  }

  public double getNominalSampleRateHz() {
    return nominalSampleRateHz;
  }

  public void setNominalSampleRateHz(double nominalSampleRateHz) {
    this.nominalSampleRateHz = nominalSampleRateHz;
  }

  public LocationDao getLocation() {
    return location;
  }

  public void setLocation(
    LocationDao location) {
    this.location = location;
  }

  public OrientationDao getOrientationAngles() {
    return orientationAngles;
  }

  public void setOrientationAngles(
    OrientationDao orientationAngles) {
    this.orientationAngles = orientationAngles;
  }

  public String getProcessingDefinition() {
    return processingDefinition;
  }

  public void setProcessingDefinition(String processingDefinition) {
    this.processingDefinition = processingDefinition;
  }

  public String getProcessingMetadata() {
    return processingMetadata;
  }

  public void setProcessingMetadata(String processingMetadata) {
    this.processingMetadata = processingMetadata;
  }

  public boolean isChannelNameEqual(ChannelDao that) {
    var channelNameEqual = false;
    if (that != null) {
      channelNameEqual = this.getName().equals(that.getName());
    }
    return channelNameEqual;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ChannelDao)) {
      return false;
    }

    ChannelDao that = (ChannelDao) o;
    return channelOrientationCode == that.channelOrientationCode &&
      Double.compare(that.nominalSampleRateHz, nominalSampleRateHz) == 0 &&
      Objects.equals(name, that.name) &&
      Objects.equals(canonicalName, that.canonicalName) &&
      Objects.equals(description, that.description) &&
      channelBandType == that.channelBandType &&
      channelInstrumentType == that.channelInstrumentType &&
      channelOrientationType == that.channelOrientationType &&
      channelDataType == that.channelDataType &&
      units == that.units &&
      Objects.equals(location, that.location) &&
      Objects.equals(orientationAngles, that.orientationAngles) &&
      Objects.equals(processingDefinition, that.processingDefinition) &&
      Objects.equals(processingMetadata, that.processingMetadata);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, canonicalName, description, channelBandType, channelInstrumentType,
      channelOrientationType, channelOrientationCode, channelDataType, units, nominalSampleRateHz,
      location, orientationAngles, processingDefinition, processingMetadata);
  }

  @Override
  public String toString() {
    return "ChannelDao{" +
      "name='" + name + '\'' +
      ", canonicalName='" + canonicalName + '\'' +
      ", description='" + description + '\'' +
      ", channelBandType=" + channelBandType +
      ", channelInstrumentType=" + channelInstrumentType +
      ", channelOrientationType=" + channelOrientationType +
      ", channelOrientationCode=" + channelOrientationCode +
      ", channelDataType=" + channelDataType +
      ", units=" + units +
      ", nominalSampleRateHz=" + nominalSampleRateHz +
      ", location=" + location +
      ", orientationAngles=" + orientationAngles +
      ", processingDefinition=" + processingDefinition +
      ", processingMetadata=" + processingMetadata +
      '}';
  }
}
